package com.namazustudios.socialengine.rt.jackson.guice;

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
import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

public class ObjectMapperPayloadReaderWriterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PayloadWriter.class).to(ObjectMapperPayloadWriter.class);
        bind(PayloadReader.class).to(ObjectMapperPayloadReader.class);
    }

}
