package com.namazustudios.socialengine.rt.jackson.guice;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadReader;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadWriter;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;

public class ObjectMapperPayloadReaderWriterModule extends AbstractModule {

    private final JsonFactory jsonFactory;

    public ObjectMapperPayloadReaderWriterModule(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    @Override
    protected void configure() {
        bind(PayloadWriter.class).to(ObjectMapperPayloadWriter.class);
        bind(PayloadReader.class).to(ObjectMapperPayloadReader.class);
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        final ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
        objectMapper.enableDefaultTyping();
        objectMapper.enableDefaultTyping(NON_FINAL);
        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return objectMapper;
    }

}
