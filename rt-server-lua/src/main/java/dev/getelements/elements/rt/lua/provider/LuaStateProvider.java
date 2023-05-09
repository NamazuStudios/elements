package dev.getelements.elements.rt.lua.provider;

import dev.getelements.elements.jnlua.Converter;
import dev.getelements.elements.jnlua.LuaState;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class LuaStateProvider implements Provider<LuaState> {

    private Provider<Converter> converterProvider;

    @Override
    public LuaState get() {
        final LuaState luaState = new LuaState();
        luaState.setConverter(getConverterProvider().get());
        return luaState;
    }

    public Provider<Converter> getConverterProvider() {
        return converterProvider;
    }

    @Inject
    public void setConverterProvider(Provider<Converter> converterProvider) {
        this.converterProvider = converterProvider;
    }

}
