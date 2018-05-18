package com.namazustudios.socialengine.rt.lua.persist;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.exception.ResourcePersistenceException;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.jnlua.LuaState.JNLUA_OBJECT;
import static com.namazustudios.socialengine.jnlua.LuaState.REGISTRYINDEX;
import static com.namazustudios.socialengine.jnlua.LuaState.RIDX_GLOBALS;

/**
 * Provides persistence support for {@link com.namazustudios.socialengine.rt.lua.LuaResource}
 */
public class Persistence {

    private static final int NULL_OBJ = -1;

    private static final byte[] SIGNATURE = new byte[]{ (byte)248, 'L', 'E', 'L', 'M', '\r', '\n' };

    private static final int VERSION_MAJOR = 1;

    private static final int VERSION_MINOR = 0;

    private static final String PERSIST_TYPE = "_t";

    private static final String PERSIST_METADATA = "_m";

    private static final String CUSTOM_PERSIST_TYPE = "_ct";

    private static final String PERSIST_JOBJECT = "__namazu_persist_jobject";

    private static final String UNPERSIST_JOBJECT = "__namazu_unpersist_jobject";

    private static final String PERMANENT_OBJECT_TABLE = "com.namazustudios.socialengine.rt.lua.persist.Persistence.PERMANENT_OBJECT_TABLE";

    private static final String INVERSE_PERMANENT_OBJECT_TABLE = "com.namazustudios.socialengine.rt.lua.persist.Persistence.INVERSE_PERMANENT_OBJECT_TABLE";

    private final Supplier<Logger> loggerSupplier;

    private final Supplier<LuaState> luaStateSupplier;

    private final Map<String, JavaFunction> customUnpersistence = new HashMap<>();

    private final Map<Object, CustomPersistenceEntry> customPersistence = new WeakHashMap<>();

    public Persistence(final Supplier<LuaState> luaStateSupplier, final Supplier<Logger> loggerSupplier) {

        this.loggerSupplier = loggerSupplier;
        this.luaStateSupplier = luaStateSupplier;

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
        try {
            final LuaState luaState = luaStateSupplier.get();
            luaState.pushJavaFunction(l -> doSerialize(l, os));
            luaState.call(0, 0);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private int doSerialize(final LuaState luaState, final OutputStream os) {

        final byte[] jObjectBytes;
        final byte[] lObjectBytes;

        try (final ByteArrayOutputStream jObjectBos = new ByteArrayOutputStream();
             final ByteArrayOutputStream lObjectBos = new ByteArrayOutputStream()) {

            final SerialObjectTable serialObjectTable = new SerialObjectTable();

            // Setup persistence
            applySpecialPersistence(luaState, serialObjectTable, l -> { throw new UnsupportedOperationException(); });

            pushPermanents(luaState);

            // Push the global table and persist it.  Gathering all Java objects in the stream
            luaState.rawGet(REGISTRYINDEX, RIDX_GLOBALS);
            luaState.persist(lObjectBos, 1, 2);

            // Persist the Java object table.
            serialObjectTable.persist(jObjectBos);

            // Collect the serial portions of each
            jObjectBytes = jObjectBos.toByteArray();
            lObjectBytes = lObjectBos.toByteArray();

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } finally {
            // If we don't clear special persistence we will not release the objects stored in the serial object
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

    }

    /**
     * Implementation for {@link Resource#deserialize(InputStream)}
     */
    public void deserialize(final InputStream is) throws IOException {
        try {
            final LuaState luaState = luaStateSupplier.get();
            luaState.pushJavaFunction(l -> doDeserialize(l, is));
            luaState.call(0, 0);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    private int doDeserialize(final LuaState luaState, final InputStream is) {

        final byte[] jObjectBytes;
        final byte[] lObjectBytes;

        try (final DataInputStream dis = new DataInputStream(is)) {

            final byte[] sig = readNBytes(dis, SIGNATURE.length);

            if (!Arrays.equals(SIGNATURE, sig)) {
                throw new ResourcePersistenceException("Invalid signature.");
            }

            final int majorVersion = dis.readInt();
            final int minorVersion = dis.readInt();

            if (majorVersion > VERSION_MAJOR || minorVersion > VERSION_MINOR) {
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
            applySpecialPersistence(luaState, l -> {throw new UnsupportedOperationException();}, deserialObjectTable);

            // Pushes the inverse version of the permanent object table.
            pushInversePermanents(luaState);
            luaState.unpersist(lObjectBis, 1);

            // Push the current glboal state on the stack and copy the result into the table.
            luaState.rawGet(REGISTRYINDEX, RIDX_GLOBALS);
            luaState.copyTable(-2, -1);

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

    private void applySpecialPersistence(final LuaState luaState,
                                         final JavaFunction persist,
                                         final JavaFunction unpersist) {

        luaState.getField(REGISTRYINDEX, JNLUA_OBJECT);

        luaState.pushJavaFunction(persist);
        luaState.setField(-2, PERSIST_JOBJECT);

        luaState.pushJavaFunction(unpersist);
        luaState.setField(-2, UNPERSIST_JOBJECT);

        luaState.getPersistenceSetting("spkey");
        luaState.load(
                // language=Lua
                "local jobject = ...\n" +
                       "local jobject_mt = getmetatable(jobject)\n" +
                       "local metadata = jobject_mt:__namazu_persist_jobject(jobject)\n" +
                       "return function()\n" +
                       "    return jobject_mt:__namazu_unpersist_jobject(metadata)\n" +
                       "end", "__namazu_special_persistence");
        luaState.setTable(-3);
        luaState.pop(1);

    }

    private void clearSpecialPersistence(final LuaState luaState) {

        luaState.getField(REGISTRYINDEX, JNLUA_OBJECT);

        luaState.pushNil();
        luaState.setField(-2, PERSIST_JOBJECT);

        luaState.pushNil();
        luaState.setField(-2, UNPERSIST_JOBJECT);

        luaState.pushNil();
        luaState.setTable(-3);

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
     * @param valueIndex the value index which will be written into the stream as a placeholder for the permanent object    
     */
    @SuppressWarnings("Duplicates")
    public void addPermanentObject(final int objectIndex, final int valueIndex) {

        final LuaState luaState = luaStateSupplier.get();

        if (luaState.isJavaFunction(objectIndex) || luaState.isJavaObjectRaw(objectIndex)) {
            throw new IllegalArgumentException("Permanent object at " + objectIndex + " must not be a Java type.");
        } else if (luaState.isJavaFunction(valueIndex) || luaState.isJavaObjectRaw(valueIndex)) {
            throw new IllegalArgumentException("Permanent object value at " + valueIndex + " must not be a Java type.");
        }

        final int absObjectIndex = luaState.absIndex(objectIndex);
        final int absValueIndex = luaState.absIndex(valueIndex);

        luaState.pushJavaFunction(l -> {
            l.getField(REGISTRYINDEX, PERMANENT_OBJECT_TABLE);
            l.insert(1);
            l.setTable(1);
            return 0;
        });
        luaState.pushValue(absObjectIndex);
        luaState.pushValue(absValueIndex);
        luaState.call(2, 0);

        luaState.pushJavaFunction(l -> {
            l.getField(REGISTRYINDEX, INVERSE_PERMANENT_OBJECT_TABLE);
            l.insert(1);
            l.setTable(1);
            return 0;
        });
        luaState.pushValue(absValueIndex);
        luaState.pushValue(absObjectIndex);
        luaState.call(2, 0);

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

        customUnpersistence.putIfAbsent(type, unpersist);

    }

    private class SerialObjectTable implements JavaFunction {

        private List<Object> objects = new ArrayList<>();

        private Map<Object, Integer> objectIndexMap = new IdentityHashMap<>();

        private int serialize(final Object object) {
            return object == null ? NULL_OBJ : objectIndexMap.computeIfAbsent(object, o -> {
                final int identifier = objects.size();
                objects.add(o);
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
                luaState.call(1, 1);
                luaState.setField(2, PERSIST_METADATA);

            }

            return 1;

        }

        public void persist(final OutputStream os) throws IOException {
            try (final ObjectOutputStream oos = new ObjectOutputStream(os)) {
                oos.writeObject(objects);
            }
        }

    }

    private class DeserialObjectTable implements JavaFunction {

        private final List<Object> objects;

        public DeserialObjectTable(final InputStream is) throws IOException {
            this(new ObjectInputStream(is));
        }

        public DeserialObjectTable(final ObjectInputStream ois) throws IOException {
            try {
                objects = (List<Object>) ois.readObject();
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

                if (NULL_OBJ == oid) {
                    luaState.pushNil();
                } else {

                    final Object object;

                    try {
                        object = objects.get(oid);
                    } catch (IndexOutOfBoundsException ex) {
                        throw new ResourcePersistenceException("Object with id does not exist: " + oid, ex);
                    }

                    luaState.pushJavaObject(object);

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
                luaState.call(1, 1);

            } else {
                throw new ResourcePersistenceException("Unknown persistence type: " + luaState.toString(-1));
            }

            return 1;

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

}
