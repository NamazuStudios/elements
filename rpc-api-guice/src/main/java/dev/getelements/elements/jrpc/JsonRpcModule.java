package dev.getelements.elements.jrpc;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.model.blockchain.BlockchainNetwork;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.annotation.RemoteModel;
import dev.getelements.elements.rt.annotation.RemoteService;
import dev.getelements.elements.rt.jrpc.JsonRpcInvocationService;
import dev.getelements.elements.rt.jrpc.JsonRpcManifestService;
import dev.getelements.elements.rt.remote.ServiceLocatorLocalInvocationDispatcher;
import dev.getelements.elements.rt.remote.LocalInvocationDispatcher;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.jrpc.JsonRpcConstants.BLOCKCHAIN_NETWORK;
import static dev.getelements.elements.jrpc.JsonRpcRedirectionStrategy.REDIRECT_URLS;
import static dev.getelements.elements.rt.SimpleJsonRpcManifestService.RPC_SERVICES;
import static dev.getelements.elements.rt.SimpleModelManifestService.RPC_MODELS;
import static dev.getelements.elements.rt.annotation.RemoteScope.*;

/**
 * Configures a JSON RPC module.
 */
public class JsonRpcModule extends PrivateModule {

    private final Logger logger = LoggerFactory.getLogger(JsonRpcModule.class);

    private ClassLoader classLoader;

    private List<Runnable> bindings = new ArrayList<>();

    public JsonRpcModule() {
        this(ClassLoader.getSystemClassLoader());
    }

    public JsonRpcModule(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    protected void configure() {
        bindings.forEach(Runnable::run);

        bind(String.class)
                .annotatedWith(named(REMOTE_PROTOCOL))
                .toInstance(ELEMENTS_JSON_RPC_PROTOCOL);

        bind(LocalInvocationDispatcher.class)
                .to(ServiceLocatorLocalInvocationDispatcher.class)
                .asEagerSingleton();

        expose(LocalInvocationDispatcher.class);

    }

    /**
     * Configures with the {@link BlockchainNetwork} instance.
     *
     * @param network the name of the network
     * @return this instance
     */
    public JsonRpcModule withNetwork(final BlockchainNetwork network) {

        bindings.add(() -> bind(BlockchainNetwork.class)
            .annotatedWith(named(BLOCKCHAIN_NETWORK))
            .toInstance(network)
        );

        return this;

    }

    /**
     * Scans for the scope, specifying both the models and services matching the scope.
     *
     * @param scope the scope
     * @return this instance
     */
    public JsonRpcModule scanningScope(final String scope) {

        bindings.add(() -> {

            final var reflections = new Reflections("dev.getelements", classLoader);

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

            bind(ModelManifestService.class)
                    .to(SimpleModelManifestService.class)
                    .asEagerSingleton();

            bind(JsonRpcManifestService.class)
                    .to(SimpleJsonRpcManifestService.class)
                    .asEagerSingleton();

            bind(JsonRpcInvocationService.class)
                    .to(SimpleJsonRpcInvocationService.class)
                    .asEagerSingleton();

            bind(String.class)
                    .annotatedWith(named(REMOTE_SCOPE))
                    .toInstance(scope);

            if (logger.isDebugEnabled()) {

                reflections
                        .getTypesAnnotatedWith(RemoteModel.class)
                        .forEach(c -> logger.debug("Got Model: {}", c));

                reflections
                        .getTypesAnnotatedWith(RemoteService.class)
                        .forEach(c -> logger.debug("Got Service: {}", c));

            }

            reflections
                    .getTypesAnnotatedWith(RemoteModel.class)
                    .stream()
                    .filter(c -> RemoteModel.Util.findScope(c, ELEMENTS_JSON_RPC_PROTOCOL, scope).isPresent())
                    .forEach(c -> models.addBinding().toInstance(c));

            reflections
                    .getTypesAnnotatedWith(RemoteService.class)
                    .stream()
                    .filter(c -> RemoteService.Util.findScope(c, ELEMENTS_JSON_RPC_PROTOCOL, scope).isPresent())
                    .forEach(c -> services.addBinding().toInstance(c));

            expose(ModelManifestService.class);
            expose(JsonRpcManifestService.class);
            expose(JsonRpcInvocationService.class);

        });

        return this;

    }

    /**
     * Disables redirection entirely.
     *
     * @return this instance
     */
    public JsonRpcModule withNoRedirect() {

        bindings.add(() -> {
            bind(JsonRpcRedirectionStrategy.class).toInstance(JsonRpcRedirectionStrategy.NO_REDIRECT);
            expose(JsonRpcRedirectionStrategy.class);
        });

        return this;

    }

    /**
     * Specifies the redirect provider.
     *
     * @return this instance
     */
    public JsonRpcModule withHttpRedirectProvider(final String redirectUrls) {

        bindings.add(() -> {

            bind(String.class)
                    .annotatedWith(named(REDIRECT_URLS))
                    .toInstance(redirectUrls);

            bind(JsonRpcRedirectionStrategy.class)
                    .to(JsonRpcHttpRedirectionStrategy.class)
                    .asEagerSingleton();

            expose(JsonRpcRedirectionStrategy.class);

        });

        return this;

    }

}
