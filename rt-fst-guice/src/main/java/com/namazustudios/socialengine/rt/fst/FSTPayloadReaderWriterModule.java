package com.namazustudios.socialengine.rt.fst;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import de.ruedigermoeller.serialization.FSTConfiguration;

import javax.inject.Provider;

public class FSTPayloadReaderWriterModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PayloadReader.class).to(FSTPayloadReader.class);
        bind(PayloadWriter.class).to(FSTPayloadWriter.class);
        bind(FSTConfiguration.class).toProvider(fstConfigurationProvider()).asEagerSingleton();
    }

    public Provider<FSTConfiguration> fstConfigurationProvider() {
        return FSTConfiguration::createDefaultConfiguration;
    }

}
