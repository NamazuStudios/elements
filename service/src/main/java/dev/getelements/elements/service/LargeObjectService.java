package dev.getelements.elements.service;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface LargeObjectService {

    LargeObject getLargeObject(String objectId);

    LargeObject createLargeObject(CreateLargeObjectRequest objectRequest);

    LargeObject updateLargeObject(UpdateLargeObjectRequest objectRequest);

    void deleteLargeObject(String objectId);

    OutputStream writeLargeObjectStream(String objectId);

    WritableByteChannel writeLargeObjectChannel(String objectId);

    InputStream readLargeObjectStream(String objectId);

    ReadableByteChannel readLargeObjectChannel(String objectId);

}
