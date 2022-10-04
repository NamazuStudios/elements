package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.remote.Invocation;

/**
 * Processes {@link JsonRpcRequest} instances and converts to {@link Invocation}.
 */
public interface JsonRpcInvocationService {


    /**
     * Processes the supplied {@link JsonRpcRequest} and converts to an {@link Invocation}.
     *
     * @param rpcRequest the {@link JsonRpcRequest}
     *
     * @return an {@link Invocation} instance
     */
    Invocation resolveInvocation(final JsonRpcRequest rpcRequest);

}
