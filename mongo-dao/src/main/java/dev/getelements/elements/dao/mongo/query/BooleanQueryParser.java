package dev.getelements.elements.dao.mongo.query;

import dev.morphia.query.Query;

import java.util.Optional;

/**
 * Translates a free-form query string to a Morphia {@link Query}.
 */
public interface BooleanQueryParser {

    /**
     * Creates a new {@link Query} from the type and the query string.
     *
     * @param cls
     * @param query
     * @return
     * @param <QueryT>
     */
    <QueryT> Optional<Query<QueryT>> parse(Class<QueryT> cls, String query);

    <QueryT> Optional<Query<QueryT>> parse(Query<QueryT> base, String query);

}
