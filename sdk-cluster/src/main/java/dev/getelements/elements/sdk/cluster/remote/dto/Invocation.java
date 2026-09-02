package dev.getelements.elements.sdk.cluster.remote.dto;

import dev.getelements.elements.sdk.cluster.address.RemoteElementMethodAddress;
import dev.getelements.elements.sdk.cluster.remote.annotation.Dispatch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a remote invocation.  This contains enough information to invoke the method remotely. This routes
 * method invocations via the {@link RemoteElementMethodAddress}.
 *
 * @param address the address of the remote method to execute.
 * @param parameters a listing of the names of the method parameters.  Each parameter is named for the {@link Class}
 *                   it represents.
 * @param arguments the arguments to pass to the remote method when invoking.
 *                  {@see {@link Method#invoke(Object, Object...)}}
 * @param dispatchType the {@link Dispatch.Type} used to send this invocation.  This can be used to hint how the
 *                     invocation can be routed.
 */
public record Invocation(
        RemoteElementMethodAddress address,
        List<String> parameters,
        List<Object> arguments,
        Dispatch.Type dispatchType) implements Serializable {}
