package com.namazustudios.socialengine.rt.jrpc;

import com.namazustudios.socialengine.rt.ResultHandlerStrategy;
import com.namazustudios.socialengine.rt.remote.Invocation;

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
