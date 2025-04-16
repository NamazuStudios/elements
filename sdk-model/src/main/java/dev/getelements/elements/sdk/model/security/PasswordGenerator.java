package dev.getelements.elements.sdk.model.security;

import dev.getelements.elements.sdk.annotation.ElementPublic;

/**
 * An interface to generate a password.
 */
@ElementPublic
public interface PasswordGenerator {

    /**
     * Generates the password and returns it.
     *
     * @return the password
     */
    String generate();

}
