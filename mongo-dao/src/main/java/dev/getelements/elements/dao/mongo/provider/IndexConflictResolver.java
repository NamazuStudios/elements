package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;

/**
 * Resolves an index-definition conflict (MongoDB error code 85 {@code IndexOptionsConflict} or
 * 86 {@code IndexKeySpecsConflict}) raised while creating an index on {@code collection}, by dropping the
 * stale, conflicting index so that it can be recreated with its newly-declared options.
 */
public interface IndexConflictResolver {

    /**
     * Attempts to resolve the given index conflict on {@code collection}.
     *
     * @param collection the collection on which the conflicting index create was attempted
     * @param cause      the exception raised by the attempted index creation
     * @return true if a conflicting index was identified and dropped; false if {@code cause} is not a
     *         recognized index-conflict error, or the conflicting index name could not be determined
     */
    boolean resolve(MongoCollection<?> collection, MongoCommandException cause);

}
