package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.util.RoundRobin;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
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
