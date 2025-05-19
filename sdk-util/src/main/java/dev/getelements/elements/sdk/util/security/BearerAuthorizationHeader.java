package dev.getelements.elements.sdk.util.security;

/**
 * Simplest type of Auth header, the bearer header.
 */
public class BearerAuthorizationHeader {

    private final String credentials;

    /**
     * Constructs an new instance of {@link BearerAuthorizationHeader} with the supplied credentials.
     * @param credentials
     */
    public BearerAuthorizationHeader(final String credentials) {
        this.credentials = credentials;
    }

    /**
     * Gets the credentials associated with this authorization header.
     *
     * @return the credentials
     */
    public String getCredentials() {
        return credentials;
    }

}
