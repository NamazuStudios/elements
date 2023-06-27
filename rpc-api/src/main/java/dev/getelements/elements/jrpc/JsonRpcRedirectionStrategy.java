package dev.getelements.elements.jrpc;

import dev.getelements.elements.rt.jrpc.JsonRpcRequest;

import java.util.function.Consumer;

/**
 * Implements the JSON-RPC Redirection Strategy.
 */
public interface JsonRpcRedirectionStrategy {

    /**
     * Names the default redirect URLs.
     */
    String REDIRECT_URLS = "dev.getelements.elements.jrpc.redirect.urls";

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
