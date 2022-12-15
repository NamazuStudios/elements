package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.exception.BadManifestException;
import com.namazustudios.socialengine.rt.manifest.model.Model;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_PROTOCOL;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.REMOTE_SCOPE;

public class SimpleModelManifestService implements ModelManifestService {

    public static final String RPC_MODELS = "com.namazustudios.socialengine.rt.rpc.model.classes";

    public static final String MODEL_PAYLOAD_READER = "com.namazustudios.socialengine.rt.rpc.simple.model.payload.reader";

    private final String scope;

    private final String protocol;

    private final Set<Class<?>> rpcModels;

    private final Validator validator;

    private final ModelIntrospector modelIntrospector;

    private final PayloadReader payloadReader;

    private final ModelManifest modelManifest;

    @Inject
    public SimpleModelManifestService(
            @Named(REMOTE_SCOPE)
            final String scope,
            @Named(REMOTE_PROTOCOL)
            final String protocol,
            @Named(RPC_MODELS)
            final Set<Class<?>> rpcModels,
            final Validator validator,
            @Named(MODEL_PAYLOAD_READER)
            final PayloadReader payloadReader,
            final ModelIntrospector modelIntrospector) {

        this.scope = scope;
        this.protocol = protocol;
        this.rpcModels = rpcModels;
        this.validator = validator;
        this.payloadReader = payloadReader;
        this.modelIntrospector = modelIntrospector;

        final var builder = new ModelManifestBuilder();
        this.modelManifest = builder.modelManifest;

    }

    @Override
    public ModelManifest getModelManifest() {
        return getPayloadReader().convert(ModelManifest.class, modelManifest);
    }

    public Validator getValidator() {
        return validator;
    }

    public ModelIntrospector getModelIntrospector() {
        return modelIntrospector;
    }

    public PayloadReader getPayloadReader() {
        return payloadReader;
    }

    private class ModelManifestBuilder {

        private final Set<String> modelNames = new HashSet<>();

        private final ModelManifest modelManifest = new ModelManifest();

        public ModelManifestBuilder() {

            final var modelsByName = new LinkedHashMap<String, Model>();

            for (var cls : rpcModels) {

                final var name = RemoteModel.Util.getName(cls);
                final var remoteScope = RemoteModel.Util.getScope(cls, protocol, scope);
                final var model = getModelIntrospector().introspectClassForModel(cls, remoteScope);

                if (modelsByName.put(name, model) != null) {
                    throw new BadManifestException("Model already exists with name: " + name);
                }

            }

            modelManifest.setModelsByName(modelsByName);

        }

    }

}
