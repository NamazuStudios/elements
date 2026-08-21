package dev.getelements.elements.rt.kryo.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.util.io.PayloadReader;
import dev.getelements.elements.sdk.util.io.PayloadWriter;
import dev.getelements.elements.sdk.cluster.kryo.KryoPayloadReader;
import dev.getelements.elements.sdk.cluster.kryo.KryoPayloadWriter;

public class KryoPayloadReaderWriterModule extends PrivateModule {

    @Override
    protected void configure() {

        expose(PayloadReader.class);
        expose(PayloadWriter.class);

        bind(PayloadReader.class).to(KryoPayloadReader.class);
        bind(PayloadWriter.class).to(KryoPayloadWriter.class);

    }

}
