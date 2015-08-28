package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.edge.EdgeClient;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 8/27/15.
 */
public class LuaEdgeResource extends AbstractLuaResource implements EdgeResource {

    @Inject
    public LuaEdgeResource(final LuaState luaState,
                           final IocResolver iocResolver,
                           final TypeRegistry typeRegistry) {
        super(luaState, iocResolver, typeRegistry);
    }

    @Override
    public EdgeRequestPathHandler getHandler(final String method) {
        return new EdgeRequestPathHandler() {
            @Override
            public Class<?> getPayloadType() {
                return getRequestType(method);
            }

            @Override
            public void handle(EdgeClient edgeClient, Request request, ResponseReceiver responseReceiver) {

            }
        };
    }
}
