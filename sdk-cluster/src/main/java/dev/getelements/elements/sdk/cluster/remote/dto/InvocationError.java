package dev.getelements.elements.sdk.cluster.remote.dto;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * Returned when there exists an error.
 *
 * @param throwable the {@link Throwable} thrown by the remote {@link Method}, or null if the method executed
 *                  successfully.
 */
public record InvocationError(Throwable throwable) implements Serializable {}
