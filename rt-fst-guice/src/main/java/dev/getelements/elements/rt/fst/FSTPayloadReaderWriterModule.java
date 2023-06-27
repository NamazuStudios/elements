package dev.getelements.elements.rt.fst;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import de.ruedigermoeller.serialization.FSTConfiguration;

import javax.inject.Provider;

public class FSTPayloadReaderWriterModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(PayloadReader.class);
        expose(PayloadWriter.class);

        bind(PayloadReader.class).to(FSTPayloadReader.class);
        bind(PayloadWriter.class).to(FSTPayloadWriter.class);

        bind(FSTConfiguration.class).toProvider(fstConfigurationProvider()).asEagerSingleton();

    }

    public Provider<FSTConfiguration> fstConfigurationProvider() {
        return FSTConfiguration::createDefaultConfiguration;
    }

}
