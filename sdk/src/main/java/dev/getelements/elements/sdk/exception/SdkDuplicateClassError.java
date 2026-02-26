package dev.getelements.elements.sdk.exception;

import dev.getelements.elements.sdk.Element;

/**
 * Thrown when an {@link Element} defines a class that exists on both the classpath of the system and the classpath
 * of the Element, and the system class is made available.
 */
public class SdkDuplicateClassError extends LinkageError {

    public SdkDuplicateClassError() {}

    public SdkDuplicateClassError(String s) {
        super(s);
    }

    public SdkDuplicateClassError(String s, Throwable cause) {
        super(s, cause);
    }

}
