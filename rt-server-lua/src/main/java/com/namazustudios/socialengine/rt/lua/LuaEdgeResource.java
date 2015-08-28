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


    private final LuaState luaState;

    @Inject
    public LuaEdgeResource(final LuaState luaState,
                           final IocResolver iocResolver,
                           final TypeRegistry typeRegistry) {
        super(luaState, iocResolver, typeRegistry);
        this.luaState = luaState;
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
                try (final StackProtector stackProtector = new StackProtector(luaState)){

                    pushRequestHandlerFunction(method);

                    luaState.pushJavaObject(edgeClient);
                    luaState.pushJavaObject(request);

                    luaState.call(3, 2);

                    final int code = (int) luaState.checkNumber(-2);
                    final Object response = luaState.checkJavaObject(-1, Object.class);

                    responseReceiver.receive(code, response);

                }
            }

        };
    }

}
