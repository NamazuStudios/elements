package dev.getelements.elements.rpc.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.jackson.ObjectMapperModelIntrospector;
import dev.getelements.elements.rt.jackson.ObjectMapperPayloadReader;
import dev.getelements.elements.rt.manifest.model.ModelIntrospector;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.SimpleJsonRpcInvocationService.INVOCATION_PAYLOAD_READER;
import static dev.getelements.elements.rt.SimpleJsonRpcManifestService.MANIFEST_PAYLOAD_READER;
import static dev.getelements.elements.rt.SimpleModelManifestService.MODEL_PAYLOAD_READER;

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
