package dev.getelements.elements.jrpc;

import dev.getelements.elements.rt.jrpc.JsonRpcRequest;
import dev.getelements.elements.sdk.model.util.RoundRobin;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Directs the client to the remote endpoint using a 307: Temporary Redirect to the underlying API.
 */
public class JsonRpcHttpRedirectionStrategy implements JsonRpcRedirectionStrategy {

    private final RoundRobin<String> urls;

    @Inject
    public JsonRpcHttpRedirectionStrategy(@Named(REDIRECT_URLS) final String urls) {
        final var urlsArray = urls.split(",");
        this.urls = new RoundRobin<>(Arrays.asList(urlsArray));
    }

    @Override
    public void redirect(
            final JsonRpcRequest jsonRpcRequest,
            final Throwable original,
            final Consumer<Object> responseConsumer,
            final Consumer<Throwable> throwableConsumer) {
        responseConsumer.accept(Response
            .status(Response.Status.TEMPORARY_REDIRECT)
            .location(URI.create(urls.next()))
            .build()
        );
    }

}
