package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.LargeObjectService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Optional;

public class UserLargeObjectService implements LargeObjectService {

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        return Optional.empty();
    }

    @Override
    public LargeObject updateLargeObject(final String objectId, final UpdateLargeObjectRequest objectRequest) {
        return null;
    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        return null;
    }

    @Override
    public void deleteLargeObject(final String objectId) {

    }

    @Override
    public InputStream readLargeObject(final String objectId) throws IOException {
        return null;
    }

    @Override
    public OutputStream writeLargeObject(final String objectId) throws IOException {
        return null;
    }

}
