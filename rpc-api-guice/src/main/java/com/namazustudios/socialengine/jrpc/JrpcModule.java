package com.namazustudios.socialengine.jrpc;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.annotation.RemoteService;
import org.reflections.Reflections;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleJsonRpcManifestService.RPC_SERVICES;
import static com.namazustudios.socialengine.rt.SimpleModelManifestService.RPC_MODELS;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.*;

/**
 * Configures a JSON RPC module.
 */
public class JrpcModule extends AbstractModule {

    private ClassLoader classLoader;

    private List<Runnable> bindings = new ArrayList<>();

    public JrpcModule() {
        this(ClassLoader.getSystemClassLoader());
    }

    public JrpcModule(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected void configure() {
        bindings.forEach(Runnable::run);
        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_JSON_RPC_HTTP_PROTOCOL);
    }

    /**
     * Configures with the {@link JsonRpcNetwork} instance.
     *
     * @param jsonRpcNetwork the JSON RPC Network
     * @return this instance
     */
    public JrpcModule withNetwork(final JsonRpcNetwork jsonRpcNetwork) {
        return scanningScope(jsonRpcNetwork.getScope()).withRedirectProvider(jsonRpcNetwork.getUrlProvider());
    }

    /**
     * Scans for the scope, specifying both the models and services matching the scope.
     *
     * @param scope the scope
     * @return this instance
     */
    public JrpcModule scanningScope(final String scope) {

        bindings.add(() -> {

            final var reflections = new Reflections("com.namazustudios", classLoader);

            final var models = newSetBinder(
                    binder(),
                    new TypeLiteral<Class<?>>() {},
                    Names.named(RPC_MODELS)
            );

            final var services = newSetBinder(
                    binder(),
                    new TypeLiteral<Class<?>>() {},
                    Names.named(RPC_SERVICES)
            );

            bind(String.class)
                    .annotatedWith(named(REMOTE_SCOPE))
                    .toInstance(scope);

            reflections
                    .getTypesAnnotatedWith(RemoteModel.class)
                    .stream()
                    .filter(c -> RemoteModel.Util.findScope(c, ELEMENTS_JSON_RPC_HTTP_PROTOCOL, scope).isPresent())
                    .forEach(c -> models.addBinding().toInstance(c));

            reflections
                    .getTypesAnnotatedWith(RemoteService.class)
                    .stream()
                    .filter(c -> RemoteService.Util.findScope(c, ELEMENTS_JSON_RPC_HTTP_PROTOCOL, scope).isPresent())
                    .forEach(c -> services.addBinding().toInstance(c));

        });

        return this;

    }

    /**
     * Specifies the redirect provider.
     *
     * @param providerClass the provider class
     * @return this instance
     */
    public JrpcModule withRedirectProvider(final Class<? extends Provider<String>> providerClass) {

        bindings.add(() -> bind(String.class)
            .annotatedWith(named(JsonRpcRedirectionStrategy.REDIRECT_URL))
            .toProvider(providerClass)
        );

        return this;

    }

}
