package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.internal.InternalRequestPathHandler;
import com.namazustudios.socialengine.rt.internal.InternalResource;

import javax.inject.Inject;
import java.util.Map;

/**
 * Created by patricktwohig on 8/27/15.
 */
public class LuaInternalResource extends AbstractLuaResource implements InternalResource {

    private final LuaState luaState;

    @Inject
    public LuaInternalResource(final LuaState luaState,
                               final IocResolver iocResolver) {
        super(luaState, iocResolver);
        this.luaState = luaState;
    }

    @Override
    public InternalRequestPathHandler getHandler(final String method) {
        return new InternalRequestPathHandler() {

            @Override
            public Class<?> getPayloadType() {
                return Map.class;
            }

            @Override
            public void handle(final Request request, final ResponseReceiver responseReceiver) {

                pushRequestHandlerFunction(method);

                luaState.pushJavaObject(request.getHeader());
                luaState.pushJavaObject(request.getPayload());
                luaState.call(2, 2);

                final int code = (int) luaState.checkNumber(-2);
                final Object payload = luaState.checkJavaObject(-1, Object.class);

                final SimpleResponse simpleResponse = SimpleResponse.builder()
                        .from(request)
                        .code(code)
                        .payload(payload)
                    .build();

                responseReceiver.receive(simpleResponse);

            }

        };
    }

}
