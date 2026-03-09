package dev.getelements.elements.sdk.model.auth;

/** Represents the body type of an HTTP request. */
public enum BodyType {
    /** No body (default for GET). */
    NONE,
    /** Form URL-encoded body (application/x-www-form-urlencoded). */
    FORM_URL_ENCODED,
    /** JSON body (application/json). */
    JSON
}

