package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.largeobject.LargeObjectNotFoundException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.Optional;

@ElementServiceExport
@ElementEventProducer(
        value = LargeObjectDao.LARGE_OBJECT_CREATED,
        parameters = LargeObject.class,
        description = "Called when a large object was created."
)
@ElementEventProducer(
        value = LargeObjectDao.LARGE_OBJECT_CREATED,
        parameters = {LargeObject.class, Transaction.class},
        description = "Called when a large object was created. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = LargeObjectDao.LARGE_OBJECT_UPDATED,
        parameters = LargeObject.class,
        description = "Called when a large object was updated."
)
@ElementEventProducer(
        value = LargeObjectDao.LARGE_OBJECT_UPDATED,
        parameters = {LargeObject.class, Transaction.class},
        description = "Called when a large object was updated. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
@ElementEventProducer(
        value = LargeObjectDao.LARGE_OBJECT_DELETED,
        parameters = LargeObject.class,
        description = "Called when a large object was deleted."
)
@ElementEventProducer(
        value = LargeObjectDao.LARGE_OBJECT_DELETED,
        parameters = {LargeObject.class, Transaction.class},
        description = "Called when a large object was deleted. This variant includes the transaction so that reactions to this event can be performed in the same transaction."
)
public interface LargeObjectDao {

    String LARGE_OBJECT_CREATED = "dev.getelements.elements.sdk.model.dao.large.object.created";

    String LARGE_OBJECT_UPDATED = "dev.getelements.elements.sdk.model.dao.large.object.updated";

    String LARGE_OBJECT_DELETED = "dev.getelements.elements.sdk.model.dao.large.object.deleted";

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
     * Gets a pagination of large objects based on the specified criteria
     * @param offset - Pagination offset
     * @param count - Pagination max objects
     * @param search - Matching substring in path or MIME type
     * @return the pagination results, if any
     */
    Pagination<LargeObject> getLargeObjects(int offset, int count, String search);

    /**
     * Creates a new {@link LargeObject}.
     *
     * @param largeObject the large object to create
     * @return the {@link LargeObject} as created in the database
     */
    LargeObject createLargeObject(LargeObject largeObject);

    /**
     * Updates an existing {@link LargeObject}.
     *
     * @param largeObject the large object to create
     * @return the {@link LargeObject} as created in the database
     */
    LargeObject updateLargeObject(LargeObject largeObject);

    /**
     * Deletes an instance of {@link LargeObject} from the database. Returning the object just before deletion.
     *
     * @param objectId the object ID
     */
    void deleteLargeObject(String objectId);

}
