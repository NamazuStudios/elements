package com.namazustudios.socialengine.rpc.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperModelIntrospector;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadReader;
import com.namazustudios.socialengine.rt.manifest.model.ModelIntrospector;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.SimpleJsonRpcInvocationService.INVOCATION_PAYLOAD_READER;
import static com.namazustudios.socialengine.rt.SimpleJsonRpcManifestService.MANIFEST_PAYLOAD_READER;
import static com.namazustudios.socialengine.rt.SimpleModelManifestService.MODEL_PAYLOAD_READER;

public class JsonRpcJacksonModule extends PrivateModule {
    @Override
    protected void configure() {

        bind(ObjectMapperPayloadReader.class).asEagerSingleton();
        bind(ObjectMapperModelIntrospector.class).asEagerSingleton();

        bind(ModelIntrospector.class)
                .to(ObjectMapperModelIntrospector.class);

        bind(PayloadReader.class)
                .annotatedWith(named(MODEL_PAYLOAD_READER))
                .to(ObjectMapperPayloadReader.class);

        bind(PayloadReader.class)
                .annotatedWith(named(MANIFEST_PAYLOAD_READER))
                .to(ObjectMapperPayloadReader.class);

        bind(PayloadReader.class)
                .annotatedWith(named(INVOCATION_PAYLOAD_READER))
                .to(ObjectMapperPayloadReader.class);

        expose(ModelIntrospector.class);
        expose(PayloadReader.class).annotatedWith(named(MODEL_PAYLOAD_READER));
        expose(PayloadReader.class).annotatedWith(named(MANIFEST_PAYLOAD_READER));
        expose(PayloadReader.class).annotatedWith(named(INVOCATION_PAYLOAD_READER));

    }
}
