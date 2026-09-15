package dev.getelements.elements.sdk.cluster.remote.dto;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Contains the result of the remote invocation.
 *
 * @param result the result of the remote {@link Method}, or null if the method failed to invoke.  Null may also
 *               indicate that the remote method returned null.
 * @param param the index of the {@code @ResultHandler}-annotated parameter this result was produced for, or
 *              {@code 0} for the single terminal result of a synchronous/future dispatch.
 */
public record InvocationResult(Object result, int param) implements Serializable {

    /**
     * Indicates that the invocation result is the return value of the method and not a parameter.
     */
    public static final int RETURN_VALUE = -1;

}
