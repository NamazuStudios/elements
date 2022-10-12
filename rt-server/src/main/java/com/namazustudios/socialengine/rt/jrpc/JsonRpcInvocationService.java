package com.namazustudios.socialengine.rt.jrpc;

import com.namazustudios.socialengine.rt.ResultHandlerStrategy;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.remote.InvocationResult;

import java.util.function.Consumer;

/**
 * Processes {@link JsonRpcRequest} instances and converts to {@link Invocation}.
 */
public interface JsonRpcInvocationService {

    /**
     * Returns an instance of {@link JsonRpcInvocation} given the {@link JsonRpcRequest}.
     *
     * @param jsonRpcRequest the {@link JsonRpcRequest}
     * @return the {@link ResultHandlerStrategy}
     */
    JsonRpcInvocation resolve(JsonRpcRequest jsonRpcRequest);

    interface JsonRpcInvocation {

        Invocation getInvocation();

        ResultHandlerStrategy getResultHandlerStrategy();

    }

}
