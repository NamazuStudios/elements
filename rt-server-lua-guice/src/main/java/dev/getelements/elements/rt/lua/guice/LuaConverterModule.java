package dev.getelements.elements.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.jnlua.Converter;
import dev.getelements.elements.jnlua.LuaState;
import dev.getelements.elements.rt.lua.converter.*;
import dev.getelements.elements.rt.lua.provider.LuaStateProvider;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;

/**
 * Created by patricktwohig on 8/18/17.
 */
public class LuaConverterModule extends AbstractModule {

    @Override
    protected void configure() {

        final Multibinder<TypedConverter> multiBinder = Multibinder.newSetBinder(binder(), TypedConverter.class);

        multiBinder.addBinding().to(CopyCollectionConverter.class);
        multiBinder.addBinding().to(HttpManifestConverter.class);
        multiBinder.addBinding().to(ModelManifestConverter.class);
        multiBinder.addBinding().to(SecurityManifestConverter.class);
        multiBinder.addBinding().to(StartupManifestConverter.class);
        multiBinder.addBinding().to(EventManifestConverter.class);

        bind(LuaState.class).toProvider(LuaStateProvider.class);
        bind(Converter.class).to(DelegatingConverter.class).asEagerSingleton();

    }

}
