package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.function.Consumer;

/**
 * Directs the client to the remote endpoint using a 307: Temporary Redirect to the underlying API.
 */
public class JsonRpcHttpRedirectionStrategy implements JsonRpcRedirectionStrategy {

    private String redirectUrl;

    @Override
    public void redirect(
            final JsonRpcRequest jsonRpcRequest,
            final Throwable original,
            final Consumer<Object> responseConsumer,
            final Consumer<Throwable> throwableConsumer) {
        responseConsumer.accept(Response
            .status(Response.Status.TEMPORARY_REDIRECT)
            .location(URI.create(getRedirectUrl()))
            .build()
        );
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    @Inject
    public void setRedirectUrl(@Named(REDIRECT_URL) String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

}
