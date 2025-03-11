package dev.getelements.elements.sdk.model.security;

/**
 * An interface to generate a password.
 */
public interface PasswordGenerator {

    /**
     * Generates the password and returns it.
     *
     * @return the password
     */
    String generate();

}
