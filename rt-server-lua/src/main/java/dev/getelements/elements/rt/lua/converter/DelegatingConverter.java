package dev.getelements.elements.rt.lua.converter;

import dev.getelements.elements.jnlua.Converter;
import dev.getelements.elements.jnlua.DefaultConverter;
import dev.getelements.elements.jnlua.LuaState;

import javax.inject.Inject;
import java.util.Set;

/**
 *
 * Created by patricktwohig on 8/18/17.
 */
public class DelegatingConverter implements Converter {

    private Set<TypedConverter> typedConverterSet;

    private static final DefaultConverter defaultConverter = DefaultConverter.getInstance();

    @Override
    public int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        return getConverterForLuaValue(luaState, index, formalType).getTypeDistance(luaState, index, formalType);
    }

    @Override
    public <T> T convertLuaValue(LuaState luaState, int index, Class<T> formalType) {
        Converter converter = getConverterForLuaValue(luaState, index, formalType);
        return converter.convertLuaValue(luaState, index, formalType);
    }

    private Converter getConverterForLuaValue(LuaState luaState, int index, Class<?> formalType) {
        return getTypedConverterSet()
            .stream()
            .filter(tc -> tc.isConvertibleFromLua(luaState, index, formalType))
            .map(tc -> (Converter) tc)
            .findFirst().orElse(defaultConverter);
    }

    @Override
    public void convertJavaObject(final LuaState luaState, final Object object) {
        getConverterForJavaValue(object).convertJavaObject(luaState, object);
    }

    private Converter getConverterForJavaValue(final Object toConvert) {

        if (toConvert == null) {
            return defaultConverter;
        }

        return getTypedConverterSet()
            .stream()
            .filter(tc -> tc.isConvertibleToLua(toConvert))
            .map(tc -> (Converter) tc)
            .findFirst().orElse(defaultConverter);
    }

    public Set<TypedConverter> getTypedConverterSet() {
        return typedConverterSet;
    }

    @Inject
    public void setTypedConverterSet(Set<TypedConverter> typedConverterSet) {
        this.typedConverterSet = typedConverterSet;
    }

}
