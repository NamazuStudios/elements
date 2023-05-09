package dev.getelements.elements.rt.jrpc;

import dev.getelements.elements.rt.ResultHandlerStrategy;
import dev.getelements.elements.rt.remote.Invocation;

/**
 * Processes {@link JsonRpcRequest} instances and converts to {@link Invocation}.
 */
public interface JsonRpcInvocationService {

    /**
     * Returns an instance of {@link InvocationResolution} given the {@link JsonRpcRequest}.
     *
     * @param jsonRpcRequest the {@link JsonRpcRequest}
     * @return the {@link ResultHandlerStrategy}
     */
    InvocationResolution resolve(JsonRpcRequest jsonRpcRequest);

    /**
     * Represents the resolved invocation which includes the {@link Invocation} and the {@link ResultHandlerStrategy}
     * used to dispatch the method call to the underlying system.
     */
    interface InvocationResolution {

        /**
         * Gets the {@link Invocation}
         *
         * @return the invocation
         */
        Invocation newInvocation();

        /**
         * Gets the {@link ResultHandlerStrategy} which defines the various
         *
         * @return the {@link ResultHandlerStrategy}
         */
        ResultHandlerStrategy newResultHandlerStrategy();

    }

}
