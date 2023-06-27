package dev.getelements.elements.rt.fst;

import dev.getelements.elements.rt.PayloadReader;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

public class FSTPayloadReader implements PayloadReader {

    private FSTConfiguration fstConfiguration;

    @Override
    public <T> T convert(Class<T> to, Object from) {
        throw new UnsupportedOperationException("Unsupported.");
    }

    @Override
    public <T> T read(final Class<T> payloadType, final InputStream stream) throws IOException {
        try {
            final FSTObjectInput fstObjectInput = getFstConfiguration().getObjectInput(stream);
            return payloadType.cast(fstObjectInput.readObject());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public <T> T read(Class<T> payloadType, byte[] toRead) throws IOException {
        try {
            return payloadType.cast(getFstConfiguration().asObject(toRead));
        } catch (Exception e) {
            throw new IOException(e);
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
