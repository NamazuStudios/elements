package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.Container;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.worker.WorkerRequestHandler;
import com.namazustudios.socialengine.rt.worker.Worker;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by patricktwohig on 8/27/15.
 */
public class LuaWorker extends AbstractLuaResource implements Worker {

    private final AtomicInteger retainCount = new AtomicInteger(1);

    private final LuaState luaState;

    private final Tabler tabler;

    @Inject
    public LuaWorker(final LuaState luaState,
                     final IocResolver iocResolver,
                     final Tabler tabler,
                     final Container<Worker> internalContainer) {
        super(luaState, iocResolver, tabler, internalContainer);
        this.luaState = luaState;
        this.tabler = tabler;
    }

    @Override
    public WorkerRequestHandler getHandler(final String method) {
        return new WorkerRequestHandler() {

            @Override
            public Class<?> getPayloadType() {
                return Map.class;
            }

            @Override
            public void handle(final Request request, final ResponseReceiver responseReceiver) {

                pushRequestHandlerFunction(method);

                luaState.pushJavaObject(request.getHeader());

                final Map requestPayload = request.getPayload(Map.class);

                if (requestPayload == null) {
                    luaState.pushNil();
                } else {
                    tabler.push(luaState, request.getPayload(Map.class));
                }

                luaState.call(2, 2);

                final SimpleResponse simpleResponse = SimpleResponse.builder()
                        .from(request)
                        .code((int) luaState.checkNumber(-2))
                        .payload(luaState.checkJavaObject(-1, Object.class))
                    .build();

                responseReceiver.receive(simpleResponse);

            }

        };
    }

}
