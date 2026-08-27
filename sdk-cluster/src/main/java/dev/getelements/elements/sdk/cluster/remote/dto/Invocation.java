package dev.getelements.elements.sdk.cluster.remote.dto;

import dev.getelements.elements.sdk.cluster.remote.annotation.Dispatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a remote invocation.  This contains enough information to invoke the method remotely.
 *
 * @param type the string representing the type of the remote object to invoke.  {@see {@link Class#getName()}}
 * @param name the name of the remote object to invoke.  {@see {@link jakarta.inject.Named}}
 * @param method the name of the remote method to invoke.  {@see {@link Method#getName()}}
 * @param parameters a listing of the names of the method parameters.  Each parameter is named for the {@link Class}
 *                   it represents.
 * @param arguments the arguments to pass to the remote method when invoking.
 *                  {@see {@link Method#invoke(Object, Object...)}}
 * @param dispatchType the {@link Dispatch.Type} used to send this invocation.  This can be used to hint how the
 *                     invocation can be routed.
 */
public record Invocation(
        String type,
        String name,
        String method,
        List<String> parameters,
        List<Object> arguments,
        Dispatch.Type dispatchType) implements Serializable {
}
