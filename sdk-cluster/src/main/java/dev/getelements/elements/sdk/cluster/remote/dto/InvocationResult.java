package dev.getelements.elements.sdk.cluster.remote.dto;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Contains the result of the remote invocation.
 *
 * @param result the result of the remote {@link Method}, or null if the method failed to invoke.  Null may also
 *               indicate that the remote method returned null.
 */
public record InvocationResult(Object result) implements Serializable {}
