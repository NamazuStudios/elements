package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;

import java.util.function.Consumer;

/**
 * Implements the JSON-RPC Redirection Strategy.
 */
public interface JsonRpcRedirectionStrategy {

    String REDIRECT_URL = "com.namazustudios.socialengine.rt.jrpc.redirect.url";

    /**
     * Redirects the JSON-RPC request.
     *  @param jsonRpcRequest the request
     * @param original the original exception
     * @param response a response handler
     * @param error a request handler
     */
    void redirect(JsonRpcRequest jsonRpcRequest, Throwable original,
                  Consumer<Object> response, Consumer<Throwable> error);

    /**
     * A simple {@link JsonRpcRedirectionStrategy} which simply re-throws the original exception, thus eliminating any
     * actual redirection.
     */
    JsonRpcRedirectionStrategy NO_REDIRECT = (jsonRpcRequest, original, response, error) -> error.accept(original);

}
