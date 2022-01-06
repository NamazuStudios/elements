package com.namazustudios.socialengine.model.auth;

/**
 * Enumeration of the private claims elements uses when processing JWT tokens.
 */
public enum PrivateClaim {

    /**
     * The auth type.
     */
    AUTH_TYPE("elm_atype"),

    /**
     * The user key.
     */
    USER_KEY("elm_ukey"),

    /**
     * The user model.
     */
    USER("elm_usr");

    private final String value;

    PrivateClaim(final String value) {
        this.value = value;
    }

    /**
     * Gets the literal valuef the claim.
     *
     * @return the literal value
     */
    public String getValue() {
        return value;
    }

}
