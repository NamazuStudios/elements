package dev.getelements.elements.rt.lua.converter;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.jnlua.LuaType;
import com.namazustudios.socialengine.jnlua.LuaValueProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.namazustudios.socialengine.jnlua.DefaultConverter.getInstance;
import static com.namazustudios.socialengine.jnlua.LuaType.TABLE;
import static dev.getelements.elements.rt.lua.Constants.MANIFEST_TYPE_METAFIELD;
import static dev.getelements.elements.rt.manifest.model.Type.*;

public class CopyCollectionConverter<ObjectT> implements TypedConverter<ObjectT> {

    private static final Logger logger = LoggerFactory.getLogger(CopyCollectionConverter.class);

    @Override
    public <T> T convertLuaValue(final LuaState luaState, final int index, final Class<T> formalType) {
        if (Iterable.class.isAssignableFrom(formalType)) {
            return (T) copyList(luaState, index);
        } else if (Map.class.isAssignableFrom(formalType)) {
            return (T) copyMap(luaState, index);
        } else if (Object.class.equals(formalType)) {
            return isArray(luaState, index) ? (T) copyList(luaState, index) : (T) copyMap(luaState, index);
        } else if (Object[].class.equals(formalType)) {
            return (T) copyArray(luaState, index);
        } else {
            final LuaType luaType = luaState.type(index);
            throw new IllegalArgumentException("Unexpected " + luaType + " on the Lua stack requested conversion.");
        }
    }

    private boolean isArray(final LuaState luaState, final int index) {

        final int top = luaState.getTop();

        try {
            if (luaState.getMetatable(index)) {
                luaState.getField(-1, MANIFEST_TYPE_METAFIELD);

                final String value = luaState.toString(-1);

                if (ARRAY.value.equals(value)) {
                    return true;
                } else if (OBJECT.value.equals(value)) {
                    return false;
                }
            }
        } finally {
            luaState.setTop(top);
        }

        try {

            // Inspect the table to see

            luaState.pushValue(index);
            luaState.pushNil();

            int count = 0;

            for (int expected = 1; luaState.next(-2); ++expected, ++count) {

                luaState.pushValue(-2);

                if (luaState.type(-1) != LuaType.NUMBER) {
                    return false;
                }

                final double actual = luaState.toNumber(-1);

                if (actual != expected) {
                    return false;
                }

                luaState.pop(2);

            }

            return count > 0;

        } finally {
            luaState.setTop(top);
        }

    }

    private Map<?, ?> copyMap(final LuaState luaState, final int index) {

        final int abs = luaState.absIndex(index);
        final Map<Object, Object> out = new LinkedHashMap<>();

        luaState.pushJavaFunction(l -> {

            l.pushNil();

            while (l.next(1)) {
                l.pushValue(-2);
                final Object k = l.toJavaObject(-1, Object.class);
                final Object v = l.toJavaObject(-2, Object.class);
                out.put(k, v);
                l.pop(2);
            }

            logger.trace("Copied Map Out.\nSize: {}\nContents: {}", out.size(), Arrays.toString(out.entrySet().toArray()));
            return 0;

        });

        luaState.pushValue(abs);
        luaState.call(1, 0);

        return out;

    }

    private List<?> copyList(final LuaState luaState, final int index) {

        final int abs = luaState.absIndex(index);
        final List<Object> out = new ArrayList<>();

        luaState.pushJavaFunction(l -> {

            final int len = luaState.rawLen(1);

            l.pushNil();

            for (int i = 0; l.next(1) && i < len; ++i) {
                l.pushValue(-2);
                final Object v = l.toJavaObject(-2, Object.class);
                out.add(v);
                l.pop(2);
            }

            logger.trace("Copied List Out.\nSize: {}\nContents: {}", out.size(), Arrays.toString(out.toArray()));
            return 0;

        });
        luaState.pushValue(abs);
        luaState.call(1, 0);

        return out;

    }

    private Object[] copyArray(final LuaState luaState, final int index) {

        final int top = luaState.getTop();
        final int abs = luaState.absIndex(index);

        try {

            final int length = luaState.rawLen(index);
            final Object[] array = new Object[length];

            luaState.pushJavaFunction(l -> {

                for (int i = 0; i < length; i++) {

                    luaState.rawGet(1, i + 1);

                    try {
                        array[i] = luaState.toJavaObject(-1, Object.class);
                    } finally {
                        luaState.pop(1);
                    }

                }

                logger.trace("Copied Array Out.\nSize: {}\nContents: {}", array.length, Arrays.toString(array));

                return 0;

            });
            luaState.pushValue(abs);
            luaState.call(1, 0);

            return array;

        } finally {
            luaState.setTop(top);
        }

    }

    @Override
    public void convertJavaObject(final LuaState luaState, final Object object) {
        if (object instanceof LuaValueProxy && luaState.equals(((LuaValueProxy) object).getLuaState())) {
            getInstance().convertJavaObject(luaState, object);
        } else if (object instanceof Map) {

            final Map<?, ?> map = (Map<?,?>)object;

            logger.trace("Copied Collection In.\nSize: {}\nContents: {}", map.size(), Arrays.toString(map.entrySet().toArray()));

            luaState.pushJavaFunction(l -> {
                l.newTable();

                map.forEach((k, v) -> {
                    l.pushJavaObject(k);
                    l.pushJavaObject(v);
                    l.setTable(-3);
                });

                l.newTable();
                l.pushString(OBJECT.value);
                l.setField(2, MANIFEST_TYPE_METAFIELD);
                l.setMetatable(1);
                l.setTop(1);

                return 1;
            });

            luaState.call(0, 1);

        } else if (object instanceof Iterable) {


            luaState.pushJavaFunction(l -> {

                int index = 0;

                final Iterable<?> list = (Iterable<?>)object;
                final Iterator<?> listIterator = list.iterator();

                l.newTable();


                final List<Object> ll = new ArrayList<>();

                while (listIterator.hasNext()) {
                    final Object element = listIterator.next();
                    l.pushJavaObject(element);
                    l.rawSet(-2, ++index);

                    ll.add(element);
                }

                logger.trace("Copied Collection In.\nSize: {}\nContents: {}", ll.size(), Arrays.toString(ll.toArray()));

                l.newTable();
                l.pushString(ARRAY.value);
                l.setField(2, MANIFEST_TYPE_METAFIELD);
                l.setMetatable(1);
                l.setTop(1);

                return 1;

            });

            luaState.call(0, 1);

        } else if (object != null && object.getClass().isArray()) {
            final Object[] array = (Object[])object;

            logger.trace("Copied Array In.\nSize: {}\nContents: {}", array.length, Arrays.toString(array));

            luaState.pushJavaFunction(l -> {

                l.newTable();

                for (int index = 0; index < array.length; ++index) {
                    final Object element = array[index];
                    l.pushJavaObject(element);
                    l.rawSet(-2, (index + 1));
                }

                l.newTable();
                l.pushString(ARRAY.value);
                l.setField(-2, MANIFEST_TYPE_METAFIELD);
                l.setMetatable(1);
                l.setTop(1);

                return 1;

            });

            luaState.call(0, 1);

        } else {
            throw new IllegalArgumentException("Unexpected object " + object + " attempted to convert.");
        }
    }

    @Override
    public boolean isConvertibleToLua(Object object) {
        return (object instanceof Iterable) || (object instanceof Map);
    }

    @Override
    public boolean isConvertibleFromLua(final LuaState luaState, final int index, final Class<?> formalType) {
        return luaState.type(index) == TABLE && (
            Map.class.isAssignableFrom(formalType) ||
            Iterable.class.isAssignableFrom(formalType) ||
            Object[].class.equals(formalType) ||
            Object.class.equals(formalType)
        );
    }

}
