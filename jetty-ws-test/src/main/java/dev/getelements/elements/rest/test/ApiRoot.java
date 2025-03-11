package dev.getelements.elements.rest.test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;

public class ApiRoot {

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    /**
     * Gets the API Root URL.
     *
     * @return the api root url
     */
    public String get() {
        return apiRoot;
    }

    /**
     * Formats the final URL to include the base URL appended to the supplied format arguments.
     *
     * @param format the format string
     * @param args the format arguments
     * @return the resulting URL
     */
    public WebTarget formatTarget(final String format, final Object ... args) {
        final var formatted = format(format, args);
        return client.target(format("%s%s", this, formatted));
    }

    /**
     * Generates a target for a pagination. The formatted string must provide a format capable accepting a query string
     * as this will automatically append &#39;offset=theOffset&amp;count=theCount&#39; but will not account for the
     * presence of &#39;&amp;&#39; or &#39;?&#39; when formatting the query string.
     *
     * @param format formats
     * @param offset the offset
     * @param count the count
     * @return the {@link WebTarget} for the requested API
     */
    public WebTarget formatPagination(final String format,
                                      final int offset, final int count,
                                      final Object ... args) {
        final var formatted = format(format, args);
        return formatTarget("%soffset=%d&count=%d", formatted, offset, count);
    }

    /**
     * Returns the literal value of the API Root, making it suitable for use in format arguments.
     *
     * @return the api root
     */
    @Override
    public String toString() {
        return get();
    }

}
