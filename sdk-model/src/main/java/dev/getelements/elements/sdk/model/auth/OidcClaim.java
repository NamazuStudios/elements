package dev.getelements.elements.sdk.model.auth;

/**
 * Enumeration of the claims elements uses when processing OIDC JWT tokens.
 */
public enum OidcClaim {

    /**
     * Key Identifier
     */
    KID("kid"),

    /**
     * Subject - A user or request id
     */
    SUB("sub"),

    /**
     * Audience - the client or application id. May be required for OAUTH2.
     */
    AUD("aud"),

    /**
     * Issuer - the issuer of the token. Typically, a URL containing the issuer's domain.
     */
    ISS("iss"),

    /**
     * Expiry - Access is disallowed after this time
     */
    EXP("exp"),

    /**
     * Not before - Access is disallowed before this time
     */
    NBF("nbf"),

    /**
     * Issued at
     */
    IAT("iat");

    private final String value;

    OidcClaim(final String value) {
        this.value = value;
    }

    /**
     * Gets the literal value of the claim.
     *
     * @return the literal value
     */
    public String getValue() {
        return value;
    }
}