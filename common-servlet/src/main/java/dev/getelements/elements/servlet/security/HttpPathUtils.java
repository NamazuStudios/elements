package dev.getelements.elements.servlet.security;

public class HttpPathUtils {

    private HttpPathUtils() {}

    /**
     * Normalizes the the path for use in servlet configuration. This will strip duplicate / characters and ensure that
     * the configured servlets will properly route calls if the input is misconfigured.
     *
     * @param input
     * @return
     */
    public static String normalize(final String input) {
        return (input.startsWith("/") ? input : "/" + input)
            .replaceAll("/{2,}", "/")
            .replaceAll("/+\\z", "");
    }

}
