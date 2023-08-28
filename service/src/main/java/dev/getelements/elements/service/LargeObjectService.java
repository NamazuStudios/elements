package dev.getelements.elements.service;

import dev.getelements.elements.exception.largeobject.LargeObjectNotFoundException;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.largeobject"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.largeobject",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )}
)
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
     * Gets an instnce of {@link LargeObject}.
     *
     * @param objectId the object ID
     * @return the {@link LargeObject}, never null
     * @throws dev.getelements.elements.exception.NotFoundException if no object exists with that supplied id
     */
    default LargeObject getLargeObject(String objectId) {
        return findLargeObject(objectId).orElseThrow(LargeObjectNotFoundException::new);
    }

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

        try (var output = writeLargeObject(objectId)) {
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

        try (var output = writeLargeObject(object.getId())) {
            inputStream.transferTo(output);
        }

        return object;

    }

    /**
     * Creates a new instance of {@link LargeObject} from the supplied {@link CreateLargeObjectRequest}.
     *
     * @param createLargeObjectRequest the large object request
     * @return the {@link LargeObject} as it was created
     * @throws IOException if there was an error writing the large object
     */
    LargeObject createLargeObject(CreateLargeObjectRequest createLargeObjectRequest);

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

        try (var output = writeLargeObject(object.getId())) {
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
    void deleteLargeObject(String objectId);

    /**
     * Opens the {@link LargeObject} for reading.
     *
     * @param objectId the object id
     * @return an InputStream used to read the object's contents
     * @throws IOException
     */
    InputStream readLargeObject(String objectId) throws IOException;

    /**
     * Opens the {@link LargeObject} for writing.
     *
     * @param objectId the object id
     * @return an OutputStream used to write the object's contents.
     * @throws IOException
     */
    OutputStream writeLargeObject(String objectId) throws IOException;

}
