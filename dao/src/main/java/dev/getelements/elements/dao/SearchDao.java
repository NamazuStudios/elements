package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;

import static java.lang.String.format;

/**
 * A generic database query type. This allows to make simplified queries for specific model types.
 */
public interface SearchDao {

    /**
     * Creates a new Query.
     *
     * @param type the type to query
     * @return the {@link SearchQuery <ModelT>}
     * @param <ModelT> the Model Type
     */
    <ModelT> SearchQuery<ModelT> query(Class<ModelT> type);

    /**
     * The entry point for the query system.
     *
     * @param <ModelT>
     */
    interface SearchQuery<ModelT> {

        /**
         * Parses the supplied query string, returning a new SearchQuery with the updated search terms.
         *
         * @param query the query
         * @return a new {@link SearchQuery with the new terms}
         */
        SearchQuery<ModelT> search(String query);

        /**
         * Performs a search for objects using the supplied format and arguments.
         *
         * {@link String#format(String, Object...)}
         *
         * @param format the format string
         * @param args the arguments
         * @return the {@link SearchQuery<ModelT>} with the updated query.
         */
        default SearchQuery<ModelT> search(final String format, final Object ... args) {
            final var query = format(format, args);
            return search(query);
        }

        /**
         * Executes the query,a ccepting the flags.
         * @param count the count
         * @param offset the offset
         * @return a {@link Pagination} with the results of the query
         */
        Pagination<ModelT> execute(int count, int offset);

    }

}
