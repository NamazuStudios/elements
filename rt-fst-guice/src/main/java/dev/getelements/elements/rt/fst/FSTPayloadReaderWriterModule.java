package dev.getelements.elements.rt.fst;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import dev.getelements.elements.rt.PayloadReader;
import dev.getelements.elements.rt.PayloadWriter;
import org.nustaq.serialization.FSTConfiguration;

import jakarta.inject.Provider;

public class FSTPayloadReaderWriterModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(PayloadReader.class);
        expose(PayloadWriter.class);

        bind(PayloadReader.class).to(FSTPayloadReader.class);
        bind(PayloadWriter.class).to(FSTPayloadWriter.class);

    }

    @Provides
    public FSTConfiguration fstConfigurationProvider() {
        return FSTConfiguration.createDefaultConfiguration();
    }

}
