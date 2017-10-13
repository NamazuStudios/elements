package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;
import com.namazustudios.socialengine.rt.lua.builtin.AssetLoaderBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/19/17.
 */
public class RTApplicationInjectorScopedProvider<ProvidedT> implements Provider<Function<Application, ProvidedT>> {

    private Function<Application, Injector> applicationInjectorFunction;

    private final Key<ProvidedT> providedTKey;


    public RTApplicationInjectorScopedProvider(final Class<ProvidedT> providedTClass) {
        this(Key.get(providedTClass));
    }

    public RTApplicationInjectorScopedProvider(final Key<ProvidedT> providedTKey) {
        this.providedTKey = providedTKey;
    }

    @Override
    public Function<Application, ProvidedT> get() {
        return application -> {
            final Injector injector = getApplicationInjectorFunction().apply(application);
            return injector.getInstance(providedTKey);
        };
    }

    public Function<Application, Injector> getApplicationInjectorFunction() {
        return applicationInjectorFunction;
    }

    @Inject
    public void setApplicationInjectorFunction(Function<Application, Injector> applicationInjectorFunction) {
        this.applicationInjectorFunction = applicationInjectorFunction;
    }

}
