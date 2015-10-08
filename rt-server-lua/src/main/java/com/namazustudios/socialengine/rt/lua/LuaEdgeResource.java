package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.edge.EdgeClientSession;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeResource;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by patricktwohig on 8/27/15.
 */
public class LuaEdgeResource extends AbstractLuaResource implements EdgeResource {

    private final LuaState luaState;

    private final Tabler tabler;

    @Inject
    public LuaEdgeResource(final LuaState luaState,
                           final IocResolver iocResolver,
                           final Tabler tabler) {
        super(luaState, iocResolver, tabler);
        this.luaState = luaState;
        this.tabler = tabler;
    }

    @Override
    public EdgeRequestPathHandler getHandler(final String method) {
        return new EdgeRequestPathHandler() {

            @Override
            public Class<?> getPayloadType() {
                return Map.class;
            }

            @Override
            public void handle(final EdgeClientSession edgeClientSession,
                               final Request request,
                               final ResponseReceiver responseReceiver) {
                try (final StackProtector stackProtector = new StackProtector(luaState)){

                    pushRequestHandlerFunction(method);

                    luaState.pushJavaObject(edgeClientSession);
                    luaState.pushJavaObject(request.getHeader());

                    final Map requestPayload = request.getPayload(Map.class);

                    if (requestPayload == null) {
                        luaState.pushNil();
                    } else {
                        tabler.push(luaState, request.getPayload(Map.class));
                    }

                    luaState.call(3, 2);

                    final SimpleResponse simpleResponse = SimpleResponse.builder()
                            .from(request)
                            .code((int)luaState.checkNumber(-2))
                            .payload(luaState.checkJavaObject(-1, Object.class))
                        .build();

                    responseReceiver.receive(simpleResponse);

                }
            }

        };
    }

}
