package dev.getelements.elements.dao;

import dev.getelements.elements.exception.largeobject.LargeObjectContentNotFoundException;
import dev.getelements.elements.model.largeobject.LargeObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Represents a bucket for storing the contents of {@link LargeObject}s.
 */
public interface LargeObjectBucket {

    /**
     * Deletes the {@link LargeObject} with the supplied id as well as the associated metadata.
     *
     * @param objectId the object id
     */
    void deleteLargeObject(String objectId) throws IOException;

    /**
     * Reads the {@link LargeObject}'s contents
     * @param objectId the object id
     * @return the {@link InputStream} which can be used to read the {@link LargeObject} contents
     * @throws IOException if an error occurs opening the contents
     * @throws LargeObjectContentNotFoundException if the content does not exist for the object.
     */
    InputStream readObject(String objectId) throws IOException;

    /**
     * Writes the {@link LargeObject}'s contents
     * @param objectId the object id
     * @return the {@link InputStream} which can be used to read the {@link LargeObject} contents
     * @throws IOException if an error occurs opening the contents
     */
    OutputStream writeObject(String objectId) throws IOException;

}
