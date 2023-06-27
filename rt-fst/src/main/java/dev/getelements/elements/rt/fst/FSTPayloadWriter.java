package dev.getelements.elements.rt.fst;

import dev.getelements.elements.rt.PayloadWriter;
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
        try {
            return getFstConfiguration().asByteArray((Serializable)payload);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    public FSTConfiguration getFstConfiguration() {
        return fstConfiguration;
    }

    @Inject
    public void setFstConfiguration(FSTConfiguration fstConfiguration) {
        this.fstConfiguration = fstConfiguration;
    }

}
