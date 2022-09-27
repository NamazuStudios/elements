package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.annotation.RemoteModel;
import com.namazustudios.socialengine.rt.manifest.model.Model;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Validator;
import java.util.LinkedHashMap;
import java.util.Set;

import static com.namazustudios.socialengine.rt.Constants.REMOTE_PROTOCOL;
import static com.namazustudios.socialengine.rt.Constants.REMOTE_SCOPE;

public class SimpleModelManifestService implements ModelManifestService {

    public static final String RPC_MODELS = "com.namazustudios.socialengine.rt.rpc.model.classes";

    private final String scope;

    private final String protocol;

    private final Set<Class<?>> rpcModels;

    private final Validator validator;

    private final ModelIntrospector modelIntrospector;

    private final Mapper mapper;

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
            final Mapper mapper,
            final ModelIntrospector modelIntrospector) {

        this.scope = scope;
        this.mapper = mapper;
        this.protocol = protocol;
        this.rpcModels = rpcModels;
        this.validator = validator;
        this.modelIntrospector = modelIntrospector;

        final var builder = new ModelManifestBuilder();
        this.modelManifest = builder.modelManifest;

    }

    @Override
    public ModelManifest getModelManifest() {
        return mapper.map(modelManifest, ModelManifest.class);
    }

    private class ModelManifestBuilder {

        private final ModelManifest modelManifest = new ModelManifest();

        public ModelManifestBuilder() {

            final var modelsByName = new LinkedHashMap<String, Model>();

            for (var cls : rpcModels) {
                final var name = RemoteModel.Util.getName(cls);
                final var remoteScope = RemoteModel.Util.getScope(cls, protocol, scope);
                final var model =modelIntrospector.introspectClassForModel(cls, remoteScope);
                modelsByName.put(name, model);
            }

            modelManifest.setModelsByName(modelsByName);

        }

    }

}
