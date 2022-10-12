package com.namazustudios.socialengine.rpc;

import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcResponse;

import java.util.function.Consumer;

/**
 * Implements the JSON-RPC Redirection Strategy.
 */
public interface JsonRpcRedirectionStrategy {

    /**
     * Redirects the JSON-RPC request.
     *
     * @param jsonRpcRequest the request
     * @param original the original exception
     * @param response a response handler
     * @param error a request handler
     */
    void redirect(JsonRpcRequest jsonRpcRequest, Throwable original,
                  Consumer<JsonRpcResponse> response, Consumer<Throwable> error);


}
