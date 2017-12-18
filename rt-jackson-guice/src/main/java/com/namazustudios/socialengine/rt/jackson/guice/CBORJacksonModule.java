package com.namazustudios.socialengine.rt.jackson.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadReader;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadWriter;

public class CBORJacksonModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(PayloadWriter.class).to(ObjectMapperPayloadWriter.class);
        bind(PayloadReader.class).to(ObjectMapperPayloadReader.class);
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper(final CBORFactory cborFactory) {
        return new ObjectMapper(cborFactory);
    }

}
