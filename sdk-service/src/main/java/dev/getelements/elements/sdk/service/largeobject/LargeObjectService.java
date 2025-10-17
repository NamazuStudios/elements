package dev.getelements.elements.sdk.service.largeobject;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.largeobject.LargeObjectNotFoundException;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface LargeObjectService {

    /**
     * Finds an instance of {@link LargeObject}. If the currently logged-in user may not access the supplied object
     * or it if it is not found, then this will return a result equivalent to {@link Optional#empty()}.
     *
     * @param objectId the object id
     * @return the {@link Optional<LargeObject>}, never null
     */
    Optional<LargeObject> findLargeObject(String objectId);

    /**
     * Gets an instance of {@link LargeObject}.
     *
     * @param objectId the object ID
     * @return the {@link LargeObject}, never null
     * @throws NotFoundException if no object exists with that supplied id
     */
    default LargeObject getLargeObject(String objectId) {
        return findLargeObject(objectId).orElseThrow(LargeObjectNotFoundException::new);
    }

    /**
     * Gets a list of large object ids and related info, but not the objects themselves.
     *
     * @return A pagination of large objects
     */
    Pagination<LargeObject> getLargeObjects(int offset, int count, String search);

    /**
     * Updates the large object's metadata only.
     *
     * @param objectId the object id
     * @param objectRequest the {@link UpdateLargeObjectRequest}
     * @return the large object
     */
    LargeObject updateLargeObject(String objectId, UpdateLargeObjectRequest objectRequest);

    /**
     * Updates the {@link LargeObject} with the supplied object id and file contents.
     *
     * @param objectId the large object's id
     * @param inputStream the input stream to read for the object's contents
     * @return the large object
     * @throws IOException if there was an error writing the large object
     */
    default LargeObject updateLargeObject(
            final String objectId,
            final InputStream inputStream) throws IOException {

        try (var output = writeLargeObjectContent(objectId)) {
            inputStream.transferTo(output);
        }

        return getLargeObject(objectId);

    }

    /**
     * Updates the {@link LargeObject} with the supplied object id, metadata, and file contents.
     *
     * @param objectId the large object's id
     * @param inputStream the input stream to read for the object's contents
     * @return the large object
     * @throws IOException if there was an error writing the large object
     */

    default LargeObject updateLargeObject(
            final String objectId,
            final UpdateLargeObjectRequest updateLargeObjectRequest,
            final InputStream inputStream) throws IOException {

        final var object = updateLargeObject(objectId, updateLargeObjectRequest);

        try (var output = writeLargeObjectContent(object.getId())) {
            inputStream.transferTo(output);
        }

        return object;

    }

    /**
     * Creates a new instance of {@link LargeObject} from the supplied {@link CreateLargeObjectRequest}.
     *
     * @param createLargeObjectRequest the large object request
     * @return the {@link LargeObject} as it was created
     */
    LargeObject createLargeObject(CreateLargeObjectRequest createLargeObjectRequest);

    /**
     * Creates a new instance of {@link LargeObject} from the supplied {@link CreateLargeObjectFromUrlRequest}.
     *
     * @param createRequest the large object request
     * @return the {@link LargeObject} as it was created
     * @throws IOException if there was an error writing the large object
     */
    LargeObject createLargeObjectFromUrl(final CreateLargeObjectFromUrlRequest createRequest) throws IOException;

    /**
     * Creates a new instance of {@link LargeObject} from the supplied {@link CreateLargeObjectRequest} and file
     * contents read from the {@link InputStream}
     *
     * @param createLargeObjectRequest the large object request
     * @return the {@link LargeObject} as it was created
     * @throws IOException if there was an error writing the large object
     */
    default LargeObject createLargeObject(
            final CreateLargeObjectRequest createLargeObjectRequest,
            final InputStream inputStream) throws IOException {

        final var object = createLargeObject(createLargeObjectRequest);

        try (var output = writeLargeObjectContent(object.getId())) {
            inputStream.transferTo(output);
        }

        return object;

    }

    /**
     * Deletes the instance of {@link LargeObject}, throw an instance of {@link LargeObjectNotFoundException} if the
     * large object does not exist or the currently logged-in user does not have permission to access.
     *
     * @param objectId the object id
     */
    void deleteLargeObject(String objectId) throws IOException;

    /**
     * Opens the {@link LargeObject} for reading.
     *
     * @param objectId the object id
     * @return an InputStream used to read the object's contents
     * @throws IOException
     */
    InputStream readLargeObjectContent(String objectId) throws IOException;

    /**
     * Opens the {@link LargeObject} for writing.
     *
     * @param objectId the object id
     * @return an OutputStream used to write the object's contents.
     * @throws IOException
     */
    OutputStream writeLargeObjectContent(String objectId) throws IOException;

    LargeObject saveOrUpdateLargeObject(LargeObject largeObject);

}
