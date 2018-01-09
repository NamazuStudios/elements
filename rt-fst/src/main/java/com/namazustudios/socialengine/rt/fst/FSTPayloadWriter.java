package com.namazustudios.socialengine.rt.fst;

import com.namazustudios.socialengine.rt.PayloadWriter;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectOutput;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

public class FSTPayloadWriter implements PayloadWriter {

    private FSTConfiguration fstConfiguration;

    @Override
    public void write(final Object payload, final OutputStream stream) throws IOException {
        final FSTObjectOutput fstObjectOutput = getFstConfiguration().getObjectOutput(stream);
        fstObjectOutput.writeObject(payload);
    }

    @Override
    public byte[] write(final Object payload) throws IOException {

        final Serializable serializable;

        try {
            serializable = (Serializable) payload;
        } catch (ClassCastException ex) {
            throw new IOException(ex);
        }

        return getFstConfiguration().asByteArray(serializable);

    }

    public FSTConfiguration getFstConfiguration() {
        return fstConfiguration;
    }

    @Inject
    public void setFstConfiguration(FSTConfiguration fstConfiguration) {
        this.fstConfiguration = fstConfiguration;
    }

}
