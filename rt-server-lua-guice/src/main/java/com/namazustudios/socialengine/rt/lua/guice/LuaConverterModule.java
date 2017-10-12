package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.naef.jnlua.Converter;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.lua.converter.DelegatingConverter;
import com.namazustudios.socialengine.rt.lua.converter.HttpManifestConverter;
import com.namazustudios.socialengine.rt.lua.converter.ModelManifestConverter;
import com.namazustudios.socialengine.rt.lua.converter.TypedConverter;
import com.namazustudios.socialengine.rt.lua.provider.LuaStateProvider;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class LuaConverterModule extends AbstractModule {

    @Override
    protected void configure() {

        final Multibinder<TypedConverter> multiBinder = Multibinder.newSetBinder(binder(), TypedConverter.class);
        multiBinder.addBinding().to(HttpManifestConverter.class);
        multiBinder.addBinding().to(ModelManifestConverter.class);

        bind(LuaState.class).toProvider(LuaStateProvider.class);
        bind(Converter.class).to(DelegatingConverter.class).asEagerSingleton();

    }

}
