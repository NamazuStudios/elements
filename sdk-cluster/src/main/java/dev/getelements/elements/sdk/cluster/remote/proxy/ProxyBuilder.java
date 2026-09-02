package dev.getelements.elements.sdk.cluster.remote.proxy;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.cluster.address.RemoteInstanceSelector;
import dev.getelements.elements.sdk.cluster.remote.MethodAssignment;
import dev.getelements.elements.sdk.cluster.remote.RemoteInvoker;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationHandler;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Builds a dynamic proxy instance of {@link ProxyT}, allowing individual methods to be assigned custom
 * {@link InvocationHandler}s (for example, to dispatch through a {@link RemoteInvoker}) while falling back to a
 * default handler for any method that is not explicitly assigned.
 *
 * @param <ProxyT> the interface type being proxied
 */
public interface ProxyBuilder<ProxyT> {

    /**
     * Ensures that interface methods declared as {@code default} are invoked directly rather than being dispatched
     * through an assigned {@link InvocationHandler}.
     *
     * @return this instance
     */
    ProxyBuilder<ProxyT> dontProxyDefaultMethods();

    /**
     * Uses the process-wide {@link SharedMethodHandleCache} to cache {@link MethodHandle} instances.
     *
     * @return this instance
     */
    ProxyBuilder<ProxyT> withSharedMethodHandleCache();

    /**
     * Specifies the {@link ServiceLocator} used to resolve services referenced while building handlers.
     *
     * @param serviceLocator the {@link ServiceLocator}
     * @return this instance
     */
    ProxyBuilder<ProxyT> withServiceLocator(ServiceLocator serviceLocator);

    /**
     * Allows for a caller to specify a remote instance selector to apply to the methods in the underlying service
     * instance.
     *
     * @param remoteInstanceSelector the instance selector
     * @return this instance
     */
    ProxyBuilder<ProxyT> withInstanceSelector(RemoteInstanceSelector remoteInstanceSelector);

    /**
     * Specifies a custom cache function used to look up, or compute and store, {@link MethodHandle} instances keyed
     * by {@link MethodHandleKey}.
     *
     * @param methodHandleCache the cache function
     * @return this instance
     */
    ProxyBuilder<ProxyT> withMethodHandleCache(BiFunction<MethodHandleKey, Supplier<MethodHandle>, MethodHandle> methodHandleCache);

    /**
     * Specifies an {@link InvocationHandler} to be assigned to one or more methods via the returned
     * {@link MethodAssignment}.
     *
     * @param invocationHandler the {@link InvocationHandler}
     * @return a {@link MethodAssignment} used to assign the handler to a specific method
     */
    MethodAssignment<ProxyBuilder<ProxyT>> handler(InvocationHandler invocationHandler);

    /**
     * Specifies the {@link InvocationHandler} used for any method that has not been explicitly assigned a handler.
     *
     * @param defaultInvocationHandler the default {@link InvocationHandler}
     * @return this instance
     */
    ProxyBuilder<ProxyT> withDefaultHandler(InvocationHandler defaultInvocationHandler);

    /**
     * Assigns a default {@link Object#toString()} implementation based on the proxied type's name.
     *
     * @return this instance
     */
    ProxyBuilder<ProxyT> withToString();

    /**
     * Assigns an {@link Object#toString()} implementation that returns the supplied value.
     *
     * @param toString the value to return from {@link Object#toString()}
     * @return this instance
     */
    ProxyBuilder<ProxyT> withToString(String toString);

    /**
     * Assigns default {@link Object#hashCode()} and {@link Object#equals(Object)} implementations based on
     * reference identity.
     *
     * @return this instance
     */
    ProxyBuilder<ProxyT> withDefaultHashCodeAndEquals();

    /**
     * Assigns an {@link InvocationHandler} for every method annotated with
     * {@link dev.getelements.elements.sdk.cluster.remote.annotation.RemotelyInvokable}, dispatching through the
     * supplied {@link RemoteInvoker}.
     *
     * @param remoteInvoker the {@link RemoteInvoker} used to dispatch remote calls
     * @return this instance
     */
    ProxyBuilder<ProxyT> withHandlersForRemoteInvoker(RemoteInvoker remoteInvoker);

    /**
     * Builds the proxy instance.
     *
     * @return the newly built {@link ProxyT} instance
     */
    ProxyT build();

}
