package com.namazustudios.socialengine.rt.lua.converter;

import com.naef.jnlua.Converter;
import com.naef.jnlua.DefaultConverter;
import com.naef.jnlua.LuaState;

import javax.inject.Inject;
import java.util.Set;

/**
 *
 * Created by patricktwohig on 8/18/17.
 */
public class DelegatingConverter implements Converter {

    private Set<TypedConverter> typedConverterSet;

    private final DefaultConverter defaultConverter = DefaultConverter.getInstance();

    @Override
    public int getTypeDistance(LuaState luaState, int index, Class<?> formalType) {
        return getConverterFor(formalType).getTypeDistance(luaState, index, formalType);
    }

    @Override
    public <T> T convertLuaValue(LuaState luaState, int index, Class<T> formalType) {
        return getConverterFor(formalType).convertLuaValue(luaState, index, formalType);
    }

    @Override
    public void convertJavaObject(LuaState luaState, Object object) {
        getConverterFor(object).convertJavaObject(luaState, object);
    }


    private Converter getConverterFor(final Object object) {
        return object == null ? defaultConverter : getConverterFor(object.getClass());
    }

    private Converter getConverterFor(final Class<?> aClass) {

        // Implement caching?

        return getTypedConverterSet()
            .stream()
            .filter(tc -> tc.isConvertibleFromLua(aClass))
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
