package dev.getelements.elements.sdk.query;

import dev.getelements.elements.sdk.ElementRegistry;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a Query type for querying within an {@link ElementRegistry}
 *
 * @param <ResultT>
 */
public interface Query<ResultT> extends Supplier<ResultT> {

    @Override
    default ResultT get() {
        return find().orElseThrow(QueryException::new);
    }

    /**
     * Gets the result of the {@link Query}
     * @return the query result
     * @throws QueryException
     */
    Optional<ResultT> find() throws QueryException;

}
