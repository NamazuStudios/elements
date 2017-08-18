package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.rt.lua.converter.DelegatingConverter;
import com.namazustudios.socialengine.rt.lua.converter.HttpManifestConverter;
import com.namazustudios.socialengine.rt.lua.converter.ModelManifestConverter;
import com.namazustudios.socialengine.rt.lua.converter.TypedConverter;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class LuaConverterModule extends AbstractModule {

    @Override
    protected void configure() {

        final Multibinder<TypedConverter> multiBinder = newSetBinder(binder(), TypedConverter.class);
        multiBinder.addBinding().to(HttpManifestConverter.class);
        multiBinder.addBinding().to(ModelManifestConverter.class);

        bind(DelegatingConverter.class);

    }

}
