package dev.getelements.elements.rt.lua.persist;

import com.google.common.collect.MapMaker;
import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaRuntimeException;
import com.namazustudios.socialengine.jnlua.LuaState;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.CurrentResource;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.rt.id.ResourceId;
import dev.getelements.elements.rt.exception.ResourcePersistenceException;
import dev.getelements.elements.rt.lua.LuaResource;
import dev.getelements.elements.rt.lua.builtin.Builtin;
import dev.getelements.elements.rt.lua.builtin.coroutine.CoroutineBuiltin;
import org.slf4j.Logger;

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.jnlua.LuaState.JNLUA_OBJECT;
import static com.namazustudios.socialengine.jnlua.LuaState.REGISTRYINDEX;
import static com.namazustudios.socialengine.jnlua.LuaState.RIDX_GLOBALS;
import static java.lang.String.format;

/**
 * Provides persistence support for {@link dev.getelements.elements.rt.lua.LuaResource}
 */
public class ErisPersistence {

    private static final int NULL_OBJ_IDX = -1;

    private static final int RESOURCE_ID_IDX = -2;

    private static final int ATTRIBUTES_IDX = -3;

    private static final byte[] SIGNATURE = new byte[]{ (byte)248, 'L', 'E', 'L', 'M', '\r', '\n' };

    private static final int VERSION_MAJOR = 1;

    private static final int VERSION_MINOR = 0;

    private static final String PERSIST_TYPE = "_t";

    private static final String PERSIST_METADATA = "_md";

    private static final String CUSTOM_PERSIST_TYPE = "_ct";

    private static final String GLOBALS = "_g";

    private static final String MODULE = "_m";

    private static final String COROUTINES = "_c";

    private static final String UNPERSIST = mangle(ErisPersistence.class, "u");

    private static final String PERMANENT_OBJECT_TABLE = mangle(ErisPersistence.class, "PERMANENT_OBJECT_TABLE");

    private static final String INVERSE_PERMANENT_OBJECT_TABLE = mangle(ErisPersistence.class, "INVERSE_PERMANENT_OBJECT_TABLE");

    private static final JavaFunction PLACEHODLER_UNPERSIST = l -> {
        // A placeholder JavaFunction which will be used in place of the actual unpersist function during
        // deserialization.  This will be swapped with the special index for UNPERSIST_CONTEXT
        throw new IllegalStateException("Cannot unpersist during persistence.");
    };

    private final LuaResource luaResource;

    private final Supplier<Logger> loggerSupplier;

    private final Supplier<LuaState> luaStateSupplier;

    private final Map<String, JavaFunction> customUnpersistence = new HashMap<>();

    private final Map<Object, CustomPersistenceEntry> customPersistence = new MapMaker().weakKeys().makeMap();

    public ErisPersistence(final LuaResource luaResource,
                           final Supplier<Logger> loggerSupplier) {

        this.luaResource = luaResource;
        this.loggerSupplier = loggerSupplier;
        this.luaStateSupplier = luaResource::getLuaState;

        final LuaState luaState = luaStateSupplier.get();

        // Creates a table in the registry to hold addCustomPersistence objects.  The table is set with weak keys such that we
        // ignore java objects that were in this table.

        luaState.newTable();
        luaState.newTable();
        luaState.pushString("k");
        luaState.setField(-2, "__mode");
        luaState.setMetatable(-2);
        luaState.setField(REGISTRYINDEX, PERMANENT_OBJECT_TABLE);

        luaState.newTable();
        luaState.newTable();
        luaState.pushString("v");
        luaState.setField(-2, "__mode");
        luaState.setMetatable(-2);
        luaState.setField(REGISTRYINDEX, INVERSE_PERMANENT_OBJECT_TABLE);

    }

    /**
     * Implementation for {@link Resource#serialize(OutputStream)}
     */
    public void serialize(final OutputStream os) throws IOException {
        try (var c = CurrentResource.getInstance().enter(luaResource)) {
            final LuaState luaState = luaStateSupplier.get();
            luaState.pushJavaFunction(l -> doSerialize(l, os));
            luaState.call(0, 0);
        } catch (LuaRuntimeException ex) {
            if (ex.getCause() instanceof UncheckedIOException) {
                throw ((UncheckedIOException) ex.getCause()).getCause();
            } else {
                throw ex;
            }
        }
    }

    private int doSerialize(final LuaState luaState, final OutputStream os) {

        final byte[] jObjectBytes;
        final byte[] lObjectBytes;

        try (var c = CurrentResource.getInstance().enter(luaResource);
             var jObjectBos = new ByteArrayOutputStream();
             var lObjectBos = new ByteArrayOutputStream()) {

            final ResourceId resourceId = luaResource.getId();
            final Attributes attributes = luaResource.getAttributes();
            final SerialObjectTable serialObjectTable = new SerialObjectTable(resourceId, attributes);

            // Setup persistence with the lua state and the table.
            applySpecialPersistence(luaState, serialObjectTable);

            // Pushes the permanent objects that have been registered with the persistence instance.  Since the above
            // method does register a permanent for the unpersist function, this must happen after applying persistence

            pushPermanents(luaState);

            luaState.newTable();

            luaState.rawGet(REGISTRYINDEX, RIDX_GLOBALS);
            luaState.setField(-2, GLOBALS);

            luaState.getField(REGISTRYINDEX, LuaResource.MODULE);
            luaState.setField(-2, MODULE);

            luaState.getField(REGISTRYINDEX, CoroutineBuiltin.COROUTINES_TABLE);
            luaState.setField(-2, COROUTINES);

            luaState.persist(lObjectBos, 1, 2);

            // Persist the Java object table.
            serialObjectTable.persist(jObjectBos);

            // Collect the serial portions of each
            jObjectBytes = jObjectBos.toByteArray();
            lObjectBytes = lObjectBos.toByteArray();

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            // If we don't clear special persistence we will not scheduleRelease the objects stored in the serial object
            // table because it will be referenced from the internals of the Lua VM.  This ensures every reference
            // is released appropriately.
            clearSpecialPersistence(luaState);
        }

        // Combine both serialized tables and write them to the stream.
        try (final DataOutputStream dos = new DataOutputStream(os)) {

            dos.write(SIGNATURE);
            dos.writeInt(VERSION_MAJOR);
            dos.writeInt(VERSION_MINOR);

            // Writes the Java object table.
            dos.writeInt(jObjectBytes.length);
            dos.write(jObjectBytes);

            // Writes the Lua object table.
            dos.writeInt(lObjectBytes.length);
            dos.write(lObjectBytes);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return 0;

    }

    private void pushPermanents(final LuaState luaState) {

        // The actual permanent Object Table
        luaState.newTable();

        // The system permanents
        luaState.pushSystemPermanents();
        luaState.copyTable(-1, -2);
        luaState.pop(1);

        // The permanent object table that's managed by this persistence context
        luaState.getField(REGISTRYINDEX, PERMANENT_OBJECT_TABLE);
        luaState.copyTable(-1, -2);
        luaState.pop(1);

        // Debugging Printout

        if (loggerSupplier.get().isTraceEnabled()) {

            luaState.pushNil();

            while (luaState.next(-2)) {
                luaState.pushValue(-2);
                luaState.pushValue(-2);
                loggerSupplier.get().info("Persistence permanents[{}]={}",
                    luaState.isString(-2) ? luaState.toString(-2) :
                    luaState.isNumber(-2) ? luaState.toNumber(-2) :
                    luaState.typeName(-2),
                    luaState.toString(-1));
                luaState.pop(3);
            }

        }

    }

    /**
     * Implementation for {@link Resource#deserialize(InputStream)}
     */
    public void deserialize(final InputStream is, final Consumer<SerialHeader> serialHeaderConsumer) throws IOException {
        try (var c = CurrentResource.getInstance().enter(luaResource)) {
            final LuaState luaState = luaStateSupplier.get();
            luaState.pushJavaFunction(l -> doDeserialize(l, is, serialHeaderConsumer));
            luaState.call(0, 0);
        } catch (LuaRuntimeException ex) {
            if (ex.getCause() instanceof UncheckedIOException) {
                throw ((UncheckedIOException) ex.getCause()).getCause();
            } else {
                throw ex;
            }
        }
    }

    private int doDeserialize(final LuaState luaState,
                              final InputStream is,
                              final Consumer<SerialHeader> serialHeaderConsumer) {

        final byte[] jObjectBytes;
        final byte[] lObjectBytes;

        try (final DataInputStream dis = new DataInputStream(is)) {

            final byte[] sig = readNBytes(dis, SIGNATURE.length);

            if (!Arrays.equals(SIGNATURE, sig)) {
                throw new ResourcePersistenceException("Invalid signature.");
            }

            final int majorVersion = dis.readInt();
            final int minorVersion = dis.readInt();

            if (majorVersion != VERSION_MAJOR || minorVersion != VERSION_MINOR) {
                throw new ResourcePersistenceException("Unable to read versions.");
            }

            int size;

            size = dis.readInt();
            jObjectBytes = readNBytes(dis, size);

            size = dis.readInt();
            lObjectBytes = readNBytes(dis, size);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        try (final ByteArrayInputStream jObjectBis = new ByteArrayInputStream(jObjectBytes);
             final ByteArrayInputStream lObjectBis = new ByteArrayInputStream(lObjectBytes)) {

            // Sets up the deserialized object table.
            final DeserialObjectTable deserialObjectTable = new DeserialObjectTable(jObjectBis);
            serialHeaderConsumer.accept(deserialObjectTable.getSerialHeader());

            // Pushes the inverse version of the permanent object table.  It is safe at this point to push the
            // derialization object table and set it to a key in this table.  This way it is strongly referenced in this
            // call.

            pushInversePermanents(luaState);
            luaState.pushJavaFunction(deserialObjectTable);
            luaState.setField(-2, UNPERSIST);

            luaState.unpersist(lObjectBis, 1);
            luaState.getField(-1, GLOBALS);
            luaState.rawGet(REGISTRYINDEX, RIDX_GLOBALS);
            luaState.copyTable(-2, -1);
            luaState.pop(2);

            luaState.getField(-1, MODULE);
            luaState.setField(REGISTRYINDEX, LuaResource.MODULE);

            luaState.getField(-1, COROUTINES);
            luaState.setField(REGISTRYINDEX, CoroutineBuiltin.COROUTINES_TABLE);

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            clearSpecialPersistence(luaState);
        }

        return 0;

    }

    private byte[] readNBytes(final InputStream is, final int desiredCount) throws IOException {

        int read;
        int totalRead = 0;
        byte[] bytes = new byte[desiredCount];

        while ((read = is.read(bytes, totalRead, desiredCount - totalRead)) >= 0 && (totalRead < desiredCount)) {
            totalRead += read;
        }

        if (totalRead < desiredCount) {
            throw new IOException("Unexpected end of stream.");
        } else if (totalRead > desiredCount) {
            loggerSupplier.get().error("Read too many bytes from stream.");
        }

        return bytes;

    }

    private void pushInversePermanents(final LuaState luaState) {

        // The actual permanent Object Table
        luaState.newTable();

        // The system permanents
        luaState.pushSystemInversePermanents();
        luaState.copyTable(-1, -2);
        luaState.pop(1);

        // The permanent object table that's managed by this persistence context
        luaState.getField(REGISTRYINDEX, INVERSE_PERMANENT_OBJECT_TABLE);
        luaState.copyTable(-1, -2);
        luaState.pop(1);

    }

    private void applySpecialPersistence(final LuaState luaState, final JavaFunction persist) {

        final int mtIndex;

        luaState.getField(REGISTRYINDEX, JNLUA_OBJECT);
        mtIndex = luaState.absIndex(-1);

        luaState.getPersistenceSetting("spkey");

        // We must assemble the actual perssistence function as a closure in Lua which will insert placeholders for
        // the actual contexts during persistence.

        luaState.load(
            // language=Lua
            "local persist, unpersist = ...\n" +
            "\n" +
            "return function (jobject)\n" +
            "\n" +
            "    local metadata = persist(jobject)\n" +
            "\n" +
            "    return function() \n" +
            "        return unpersist(metadata)\n" +
            "    end\n" +
            "\n" +
            "end\n", "__jvm_persist");

        luaState.pushJavaFunction(persist);

        luaState.pushJavaFunction(PLACEHODLER_UNPERSIST);
        luaState.pushString(UNPERSIST);
        doAddPermanentObject(luaState, -2, -1);
        luaState.pop(1);

        // With both functions added to the scope, we execute the function which will pop both the functtions off the
        // stack as well as the compiled chunk.  The compiled chunk returns the actual spio function which encapsulates
        // the persist/unpersist methods.  Which will be replaced throught he permanent object table.

        try (var c = CurrentResource.getInstance().enter(luaResource)) {
            luaState.call(2, 1);
        }

        // Sets it to the result of the above function.
        luaState.setTable(mtIndex);

        // Pops off the jobject metatable
        luaState.pop(1);

    }

    private void clearSpecialPersistence(final LuaState luaState) {
        luaState.getField(REGISTRYINDEX, JNLUA_OBJECT);
        luaState.getPersistenceSetting("spkey");
        luaState.pushNil();
        luaState.setTable(-3);
        luaState.pop(1);
    }

    /**
     * Adds a permanent object using {@link #addPermanentObject(int, Class, String)} by specifying the object
     * placeholder as the result of using {@link #mangle(Class, String)}
     *
     * @param objectIndex the index on the lua stack
     * @param scope the enclosing scope
     * @param name the name
     */
    public void addPermanentObject(final int objectIndex, final Class<?> scope, final String name) {
        final LuaState luaState = luaStateSupplier.get();
        final int absObjectIndex = luaState.absIndex(objectIndex);
        luaState.pushString(mangle(scope, name));
        addPermanentObject(absObjectIndex, -1);
        luaState.pop(1);
    }

    /**
     * Adds the object, its value, and inverse value to the permanent object table tracked in the registry.  The values
     * specified here are then just in time for serialization they are assembled into a table representing permanent
     * objects.  The object specified in this method will not be serialized, rather the object at the value index
     * will take its place in the stream during serizliation.  In the reverse direction, the object will replace the
     * stream during deserialization.
     *
     * Note that the object specified should not be a Java object because of how Lua internally tracks Java objects.  To
     * specify special serialization, use {@link #addCustomUnpersistence(String, JavaFunction)}.
     *
     * @param objectIndex the stack index of the permanent object
     * @param placeholderIndex the value index which will be written into the stream as a placeholder for the permanent object
     */
    public void addPermanentObject(final int objectIndex, final int placeholderIndex) {

        final LuaState luaState = luaStateSupplier.get();

        if (luaState.isJavaFunction(objectIndex) || luaState.isJavaObjectRaw(objectIndex)) {
            throw new IllegalArgumentException("Permanent object at " + objectIndex + " must not be a Java type.");
        } else if (luaState.isJavaFunction(placeholderIndex) || luaState.isJavaObjectRaw(placeholderIndex)) {
            throw new IllegalArgumentException("Permanent object placeholder at " + placeholderIndex + " must not be a Java type.");
        }

        doAddPermanentObject(luaState, objectIndex, placeholderIndex);

    }

    @SuppressWarnings("Duplicates")
    private void doAddPermanentObject(final LuaState luaState, final int objectIndex, final int placeholderIndex) {

        final int absObjectIndex = luaState.absIndex(objectIndex);
        final int absPlaceholderIndex = luaState.absIndex(placeholderIndex);

        luaState.pushJavaFunction(l -> {
            l.getField(REGISTRYINDEX, PERMANENT_OBJECT_TABLE);
            l.insert(1);
            l.setTable(1);
            return 0;
        });
        luaState.pushValue(absObjectIndex);
        luaState.pushValue(absPlaceholderIndex);

        try (var c = CurrentResource.getInstance().enter(luaResource)) {
            luaState.call(2, 0);
        }

        luaState.pushJavaFunction(l -> {
            l.getField(REGISTRYINDEX, INVERSE_PERMANENT_OBJECT_TABLE);
            l.insert(1);
            l.setTable(1);
            return 0;
        });
        luaState.pushValue(absPlaceholderIndex);
        luaState.pushValue(absObjectIndex);

        try (var c = CurrentResource.getInstance().enter(luaResource)) {
            luaState.call(2, 0);
        }

    }

    /**
     * Adds a {@link JavaFunction} which can be used for custom Java object persistence.  The supplied
     * {@link JavaFunction} instances will be used in the process of serializing the supplied object.
     *
     * Ths supplied object is retained weakly, so there is no need to deregister th object later.
     *
     * The persist function must accept no arguments and return a single unique object on the Lua stack.  The returned
     * type may be any Lua type that is serializable.
     *
     * @param instance the instance to persist, must not be null
     * @param persist the {@link JavaFunction} which will be used to persist the object.
     * @param <T> the type to persist.
     *
     * @return true if an existing custom persistence strategy was registered to the mapping
     */
    public <T> boolean addCustomPersistence(final T instance, final String type, final JavaFunction persist) {

        if (instance == null) {
            throw new IllegalArgumentException("Instance must not be null.");
        } else if (!customUnpersistence.containsKey(type)) {
            throw new IllegalArgumentException("Unknown custom persistence type: " + type);
        }

        return customPersistence.putIfAbsent(instance, new CustomPersistenceEntry(type, persist)) != null;

    }

    /**
     * Adds a {@link JavaFunction} that is used for custom unpersistence.  While traversing the object graph, the
     * persistence layer will check if the persisted metadata is of custom type.  If it is, this will invoke the
     * supplied function.  The function will accept the persistence metadata provided by the associated
     * {@link JavaFunction} passed to {@link #addCustomPersistence(Object, String, JavaFunction)}.
     *
     * @param unpersist the {@link JavaFunction} which will be used to un-persist the object.
     */
    public void addCustomUnpersistence(final String type, final JavaFunction unpersist) {

        if (type == null) {
            throw new IllegalArgumentException("Invalid persistence type: " + type);
        } else if (unpersist == null) {
            throw new IllegalArgumentException("Invalid persistence function: " + unpersist);
        }

        if (customUnpersistence.putIfAbsent(type, unpersist) != null) {
            loggerSupplier.get().warn("Custom persistence already registered for type: " + type);
        }

    }

    /**
     * A shortcut to use {@link #addCustomPersistence(Object, String, JavaFunction)} and
     * {@link #addCustomUnpersistence(String, JavaFunction)} for a specific java object.  This is useful for objects
     * provided in {@link Builtin} instances.  This uses {@link #mangle(Class, String)} to generate the name of the
     * object and the enclosing scope.
     *
     * {@see {@link #mangle(Class, String)}}
     *
     * @param object the object itself
     * @param scope the {@link Class} enclosing the object
     * @param name the name of the object
     */
    public void addPermanentJavaObject(final Object object, final Class<?> scope, final String name) {

        final String persistenceType = mangle(scope, name);
        final WeakReference<Object> objectWeakReference = new WeakReference<>(object);

        addCustomUnpersistence(persistenceType, l -> {
            final Object target = objectWeakReference.get();

            // This check is here strictly for sanity and should warn.  This would likely happen if Lua itself
            // is prematurely releasing global refs in JNI.  However, we shoudl log an error if it does somehow happen
            // as to warrant further investigation into the issue.

            if (target == null) {
                loggerSupplier.get().error("Object {} was garbage collected.", persistenceType);
            }

            l.pushJavaObjectRaw(target);
            return 1;

        });

        addCustomPersistence(object, persistenceType, l -> {
            l.pushNil();
            return 1;
        });

    }

    private class SerialObjectTable implements JavaFunction {

        private SerialHeader serialHeader;

        private Map<Object, Integer> objectIndexMap = new IdentityHashMap<>();

        public SerialObjectTable(final ResourceId resourceId, final Attributes attributes) {
            this.serialHeader = new SerialHeader();
            serialHeader.setResourceId(resourceId);
            serialHeader.setAttributes(attributes);
            serialHeader.setObjectTable(new ArrayList<>());
        }

        private int serialize(final Object object) {

            if (object == this) {
                throw new IllegalStateException("Cannot persist the Java object table.");
            }

            return object == null ? NULL_OBJ_IDX :
                   object == serialHeader.getResourceId() ? RESOURCE_ID_IDX :
                   object == serialHeader.getAttributes() ? ATTRIBUTES_IDX :
                   objectIndexMap.computeIfAbsent(object, o -> {
                       final int identifier = serialHeader.getObjectTable().size();
                       serialHeader.getObjectTable().add(o);
                       return identifier;
                   });

        }

        @Override
        public int invoke(final LuaState luaState) {

            final Object object = luaState.toJavaObjectRaw(1);
            final CustomPersistenceEntry custom = customPersistence.get(object);

            if (luaState.getTop() != 1) {
                throw new IllegalArgumentException("Persist method must accept a single argument.");
            }

            luaState.newTable();

            if (custom == null) {

                final int identifier = serialize(object);

                luaState.pushString(PersistType.JVM.toString());
                luaState.setField(2, PERSIST_TYPE);

                luaState.pushInteger(identifier);
                luaState.setField(2, PERSIST_METADATA);

            } else {

                luaState.pushString(PersistType.CUSTOM.toString());
                luaState.setField(2, PERSIST_TYPE);

                luaState.pushString(custom.type);
                luaState.setField(2, CUSTOM_PERSIST_TYPE);

                luaState.pushJavaFunction(custom.persist);
                luaState.pushValue(1);

                try (var c = CurrentResource.getInstance().enter(luaResource)) {
                    luaState.call(1, 1);
                }

                luaState.setField(2, PERSIST_METADATA);

            }

            if (loggerSupplier.get().isTraceEnabled()) {

                luaState.pushNil();

                while (luaState.next(-2)) {
                    luaState.pushValue(-2);
                    luaState.pushValue(-2);
                    loggerSupplier.get().trace("Saving prsistence metadata for {}: {}[{}]={}",
                        object == null ? null : object.getClass().getSimpleName(),
                        object,
                        luaState.toString(-2),
                        luaState.isString(-1) ? luaState.toString(-1) :
                        luaState.isNumber(-1) ? luaState.toNumber(-1) :
                        luaState.typeName(-1));
                    luaState.pop(3);
                }

            }

            return 1;

        }

        public void persist(final OutputStream os) throws IOException {

            final Logger logger = loggerSupplier.get();

            serialHeader.getObjectTable().stream()
               .filter(o -> !(o instanceof Serializable))
               .forEach(o -> logger.error("{} is not and instance of {}", o, Serializable.class.getName()));

            try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
                oos.writeObject(serialHeader);
            }

        }

    }

    private class DeserialObjectTable implements JavaFunction {

        final SerialHeader serialHeader;

        public DeserialObjectTable(final InputStream is) throws IOException {
            try (final ObjectInputStream ois = new ObjectInputStream(is)) {
                serialHeader = (SerialHeader) ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }

        @Override
        public int invoke(final LuaState luaState) {

            if (luaState.getTop() != 1) {
                throw new IllegalArgumentException("Function takes exactly one argument.");
            }

            final PersistType type;
            luaState.getField(1, PERSIST_TYPE);
            type = luaState.checkEnum(-1, PersistType.values());
            luaState.pop(1);

            luaState.getField(1, PERSIST_METADATA);

            if (PersistType.JVM.equals(type)) {

                final int oid = luaState.toInteger(2);

                if (NULL_OBJ_IDX == oid) {
                    luaState.pushNil();
                } else if (RESOURCE_ID_IDX == oid) {
                    luaState.pushJavaObjectRaw(serialHeader.getResourceId());
                } else if (ATTRIBUTES_IDX == oid) {
                    luaState.pushJavaObjectRaw(serialHeader.getAttributes());
                } else {

                    final Object object;

                    try {
                        object = serialHeader.getObjectTable().get(oid);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ResourcePersistenceException("Object with id does not exist: " + oid, ex);
                    }

                    luaState.pushJavaObjectRaw(object);

                }

            } else if (PersistType.CUSTOM.equals(type)) {

                final String customPersistType;
                luaState.getField(1, CUSTOM_PERSIST_TYPE);
                customPersistType = luaState.toString(-1);
                luaState.pop(1);

                final JavaFunction unpersist = customUnpersistence.get(customPersistType);

                if (unpersist == null) {
                    throw new ResourcePersistenceException("No unpersistence registered for type: " + unpersist);
                }

                luaState.pushJavaFunction(unpersist);
                luaState.pushValue(2);

                try (var c = CurrentResource.getInstance().enter(luaResource)) {
                    luaState.call(1, 1);
                }

            } else {
                throw new ResourcePersistenceException("Unknown persistence type: " + luaState.toString(-1));
            }

            return 1;

        }

        public SerialHeader getSerialHeader() {
            return serialHeader;
        }

    }

    private static class CustomPersistenceEntry {

        public final String type;

        public final JavaFunction persist;

        public CustomPersistenceEntry(final String type, final JavaFunction persist) {

            if (type == null) {
                throw new IllegalArgumentException("Invalid persistence type: " + type);
            } else if (persist == null) {
                throw new IllegalArgumentException("Invalid persistence function: " + persist);
            }

            this.type = type;
            this.persist = persist;

        }

    }

    /**
     * The serialized type of the metadata.  When serializing java objects, this determines how to actually read the
     * value from the stream.
     */
    private enum PersistType {

        /**
         *
         */
        JVM,

        /**
         *
         */
        CUSTOM

    }

    /**
     * Mangles the provided name for the enclsoing class.  This is a convenience method used to ensure that the provided
     * name is unique by incorporating the {@link Class}'s canonical name in the placeholder.
     *
     * @param scope the scope
     * @param name the name
     * @return the mangled result
     */
    public static String mangle(final Class<?> scope, final String name) {
        return format("%s.%s", scope.getCanonicalName(), name);
    }

}
