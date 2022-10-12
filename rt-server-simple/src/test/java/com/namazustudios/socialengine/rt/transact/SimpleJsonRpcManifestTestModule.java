package com.namazustudios.socialengine.rt.transact;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperModelIntrospector;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadReader;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcManifestService;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;

import java.util.function.Consumer;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.namazustudios.socialengine.rt.SimpleJsonRpcManifestService.RPC_SERVICES;
import static com.namazustudios.socialengine.rt.SimpleModelManifestService.RPC_MODELS;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.*;

public abstract class SimpleJsonRpcManifestTestModule extends AbstractModule {

    public static final String FAILURE = "failure";

    public static final String HAPPY_SCOPE = "happy";

    private static <T> Consumer<T> illegalState() {
        return _ignored -> {
            throw new IllegalStateException("Can only call during configure()");
        };
    }

    private String scope = HAPPY_SCOPE;

    private Consumer<String> acceptScope = illegalState();

    private Consumer<Class<?>> acceptModel = illegalState();

    private Consumer<Class<?>> acceptService = illegalState();

    @Override
    protected final void configure() {

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

        bind(PayloadReader.class).to(ObjectMapperPayloadReader.class);
        bind(ModelIntrospector.class).to(ObjectMapperModelIntrospector.class);
        bind(ModelManifestService.class).to(SimpleModelManifestService.class);
        bind(JsonRpcManifestService.class).to(SimpleJsonRpcManifestService.class);

        services.addBinding().toInstance(TestJsonRpcServiceSimple.class);
        services.addBinding().toInstance(TestJsonRpcServiceModelParameters.class);

        try {
            acceptScope = s -> scope = s;
            acceptModel = c -> models.addBinding().toInstance(c);
            acceptService = c -> services.addBinding().toInstance(c);
            configureTypes();
        } finally {
            acceptScope = illegalState();
            acceptModel = illegalState();
            acceptService = illegalState();
        }

        bind(String.class).annotatedWith(Names.named(REMOTE_SCOPE)).toInstance(scope);
        bind(String.class).annotatedWith(Names.named(REMOTE_PROTOCOL)).toInstance(ELEMENTS_JSON_RPC_HTTP_PROTOCOL);

    }

    protected abstract void configureTypes();

    protected final SimpleJsonRpcManifestTestModule withScope(final String scope) {
        acceptScope.accept(scope);
        return this;
    }

    protected final SimpleJsonRpcManifestTestModule bindModel(final Class<?> cls) {
        acceptModel.accept(cls);
        return this;
    }

    protected final SimpleJsonRpcManifestTestModule bindService(final Class<?> cls) {
        acceptService.accept(cls);
        return this;
    }


}
