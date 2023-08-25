package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.LargeObjectService;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class UserLargeObjectService implements LargeObjectService {

    @Override
    public LargeObject createLargeObject(CreateLargeObjectRequest objectRequest) {
        return null;
    }

    @Override
    public LargeObject updateLargeObject(UpdateLargeObjectRequest objectRequest) {
        return null;
    }

    @Override
    public LargeObject getLargeObject(String objectId) {
        return null;
    }

    @Override
    public void deleteLargeObject(String objectId) {

    }

    @Override
    public OutputStream writeLargeObjectStream(String objectId) {
        return null;
    }

    @Override
    public WritableByteChannel writeLargeObjectChannel(String objectId) {
        return null;
    }

    @Override
    public InputStream readLargeObjectStream(String objectId) {
        return null;
    }

    @Override
    public ReadableByteChannel readLargeObjectChannel(String objectId) {
        return null;
    }
    
}
