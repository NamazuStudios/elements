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
     * @param cls the query type
     * @param query the query
     * @return the {@link Optional<Query>} if the query successfully parsed
     * @param <QueryT>
     */
    <QueryT> Optional<Query<QueryT>> parse(Class<QueryT> cls, String query);

    /**
     * Parses the supplied query and converts to the supplied {@link Query}
     *
     * @param base the base query
     * @param query the query
     * @return the {@link Optional<Query>} if the query successfully parsed
     * @return
     * @param <QueryT>
     */
    <QueryT> Optional<Query<QueryT>> parse(Query<QueryT> base, String query);

}
