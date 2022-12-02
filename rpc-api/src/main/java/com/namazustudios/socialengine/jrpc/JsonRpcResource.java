package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.rt.Subscription;
import com.namazustudios.socialengine.rt.exception.MethodNotFoundException;
import com.namazustudios.socialengine.rt.exception.ServiceNotFoundException;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcInvocationService;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcManifestService;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.manifest.jrpc.JsonRpcManifest;
import com.namazustudios.socialengine.rt.remote.LocalInvocationDispatcher;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("jrpc")
public class JsonRpcResource {

    private JsonRpcManifestService jsonRpcManifestService;

    private JsonRpcInvocationService jsonRpcInvocationService;

    private JsonRpcRedirectionStrategy jsonRpcRedirectionStrategy;

    private LocalInvocationDispatcher localInvocationDispatcher;

    @GET
    @Path("invoke")
    @Consumes({
        APPLICATION_JSON,
        "application/json-rpc",
        "application/jsonrequest"
    })
    @Produces({
        APPLICATION_JSON,
        "application/json-rpc",
        "application/jsonrequest"
    })
    public void invoke(
            @QueryParam("jsonrpc")
            @DefaultValue(JsonRpcRequest.V_2_0)
            final String jsonrpc,
            @QueryParam("method")
            final String method,
            @QueryParam("params")
            final String params,
            @QueryParam("id")
            final String id,
            @Suspended
            final AsyncResponse asyncResponse) {
        final var parsedParams = parse(params);
        final var jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setId(id);
        jsonRpcRequest.setMethod(method);
        jsonRpcRequest.setJsonrpc(jsonrpc);
        jsonRpcRequest.setParams(parsedParams);
        invoke(jsonRpcRequest, asyncResponse);
    }

    private static Object parse(final String params) {

        if (params == null) return null;

        try {
            return Double.parseDouble(params);
        } catch (NumberFormatException ignored) {}

        try {
            return Long.parseLong(params);
        } catch (NumberFormatException ignored) {}

        if ("true".equals(params)) {
            return Boolean.TRUE;
        } else if ("false".equals(params)) {
            return Boolean.FALSE;
        } else {
            return params;
        }

    }

    @POST
    @Path("invoke")
    @Consumes({
        APPLICATION_JSON,
        "application/json-rpc",
        "application/jsonrequest"
    })
    @Produces({
        APPLICATION_JSON,
        "application/json-rpc",
        "application/jsonrequest"
    })
    public void invoke(
            final JsonRpcRequest jsonRpcRequest,
            @Suspended
            final AsyncResponse asyncResponse) {

        final JsonRpcInvocationService.InvocationResolution resolution;

        try {
            resolution = getJsonRpcInvocationService().resolve(jsonRpcRequest);
        } catch (MethodNotFoundException | ServiceNotFoundException original) {

            getJsonRpcRedirectionStrategy().redirect(
                jsonRpcRequest,
                original,
                asyncResponse::resume,
                asyncResponse::resume
            );

            return;

        }

        final var resultHandlerStrategy = resolution.newResultHandlerStrategy();

        final var subscription = Subscription.begin()
            .chain(resultHandlerStrategy.onError(asyncResponse::resume))
            .chain(resultHandlerStrategy.onFinalResult(asyncResponse::resume));

        asyncResponse.setTimeoutHandler(ar -> subscription.unsubscribe());
        getLocalInvocationDispatcher().dispatch(resolution.newInvocation(), resultHandlerStrategy);

    }

    @GET
    @Path("manifest")
    @Produces(APPLICATION_JSON)
    public JsonRpcManifest getManifest() {
        return getJsonRpcManifestService().getJsonRpcManifest();
    }

    public JsonRpcManifestService getJsonRpcManifestService() {
        return jsonRpcManifestService;
    }

    @Inject
    public void setJsonRpcManifestService(JsonRpcManifestService jsonRpcManifestService) {
        this.jsonRpcManifestService = jsonRpcManifestService;
    }

    public JsonRpcInvocationService getJsonRpcInvocationService() {
        return jsonRpcInvocationService;
    }

    @Inject
    public void setJsonRpcInvocationService(JsonRpcInvocationService jsonRpcInvocationService) {
        this.jsonRpcInvocationService = jsonRpcInvocationService;
    }

    public JsonRpcRedirectionStrategy getJsonRpcRedirectionStrategy() {
        return jsonRpcRedirectionStrategy;
    }

    @Inject
    public void setJsonRpcRedirectionStrategy(JsonRpcRedirectionStrategy jsonRpcRedirectionStrategy) {
        this.jsonRpcRedirectionStrategy = jsonRpcRedirectionStrategy;
    }

    public LocalInvocationDispatcher getLocalInvocationDispatcher() {
        return localInvocationDispatcher;
    }

    @Inject
    public void setLocalInvocationDispatcher(LocalInvocationDispatcher localInvocationDispatcher) {
        this.localInvocationDispatcher = localInvocationDispatcher;
    }

}
