package dev.getelements.elements.dao;

import dev.getelements.elements.exception.largeobject.LargeObjectNotFoundException;
import dev.getelements.elements.model.largeobject.LargeObject;

import java.util.Optional;

public interface LargeObjectDao {

    /**
     * Finds a {@link LargeObject} with the supplied object id, returning a value equivalent to {@link Optional#empty()}
     * if the object cannot be found.
     *
     * @param objectId the object id
     * @return the {@link Optional<LargeObject>}
     */
    Optional<LargeObject> findLargeObject(String objectId);

    /**
     * Finds a {@link LargeObject} with the supplied object id, throwing an instance of
     * {@link LargeObjectNotFoundException} of the object cannot be found.
     *
     * @param objectId the object id
     * @return the {@link Optional<LargeObject>}
     */
    default LargeObject getLargeObject(String objectId) {
        return findLargeObject(objectId).orElseThrow(LargeObjectNotFoundException::new);
    }

    /**
     * Creates a new {@link LargeObject}.
     *
     * @param largeObject the large object to create
     *
     * @return the {@link LargeObject} as created in the database
     */
    LargeObject createLargeObject(LargeObject largeObject);

    /**
     * Updates an existing {@link LargeObject}.
     *
     * @param largeObject the large object to create
     *
     * @return the {@link LargeObject} as created in the database
     */
    LargeObject updateLargeObject(LargeObject largeObject);

    /**
     * Deletes an instance of {@link LargeObject} from the database. Returning the object just before deletion.
     *
     * @param objectId the object ID
     * @return the {@link LargeObject}
     */
    LargeObject deleteLargeObject(String objectId);

}
