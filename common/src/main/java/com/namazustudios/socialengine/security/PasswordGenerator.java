package com.namazustudios.socialengine.security;

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
