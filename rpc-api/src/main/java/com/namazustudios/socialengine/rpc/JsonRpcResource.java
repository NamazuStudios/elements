package com.namazustudios.socialengine.rpc;

import com.namazustudios.socialengine.rt.jrpc.JsonRpcRequest;
import com.namazustudios.socialengine.rt.remote.LocalInvocationDispatcher;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class JsonRpcResource {

    private LocalInvocationDispatcher localInvocationDispatcher;

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public void invoke(
            final JsonRpcRequest jsonRpcRequest,
            @Suspended final AsyncResponse asyncResponse) {
                
    }

    public LocalInvocationDispatcher getLocalInvocationDispatcher() {
        return localInvocationDispatcher;
    }

    @Inject
    public void setLocalInvocationDispatcher(LocalInvocationDispatcher localInvocationDispatcher) {
        this.localInvocationDispatcher = localInvocationDispatcher;
    }

}
