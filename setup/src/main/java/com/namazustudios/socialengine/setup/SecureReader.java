package com.namazustudios.socialengine.setup;

/**
 * Securely reads the prompt from a shell (without echoing what is typed).
 */
public interface SecureReader {

    /**
     * Reads a single line from the terminal. Providing hte supplied prompt.
     *
     * {@see {@link String#format(String, Object...)}}
     * @param fmt the format string
     * @param args the format string args
     *
     * @return the string that was read from the terminal
     */
    String reads(String fmt, Object... args);

}
