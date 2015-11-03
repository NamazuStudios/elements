package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.Server;
import com.namazustudios.socialengine.rt.SimpleResponse;
import com.namazustudios.socialengine.rt.internal.InternalRequestPathHandler;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import com.namazustudios.socialengine.rt.internal.InternalServer;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by patricktwohig on 8/27/15.
 */
public class LuaInternalResource extends AbstractLuaResource implements InternalResource {

    private final AtomicInteger retainCount = new AtomicInteger(1);

    private final LuaState luaState;

    private final Tabler tabler;

    private final InternalServer internalServer;

    @Inject
    public LuaInternalResource(final LuaState luaState,
                               final IocResolver iocResolver,
                               final Tabler tabler,
                               final InternalServer internalServer) {
        super(luaState, iocResolver, tabler);
        this.luaState = luaState;
        this.tabler = tabler;
        this.internalServer = internalServer;
    }

    @Override
    public int retain() {

        if (retainCount.get() <= 0) {
            throw new ZeroReferenceCountException("Retain count: " + retainCount.get());
        }

        return retainCount.incrementAndGet();

    }

    @Override
    public int release() {

        if (retainCount.get() <= 0) {
            throw new ZeroReferenceCountException("Retain count: " + retainCount.get());
        }

        final int count =  retainCount.decrementAndGet();

        if (count == 0) {
            internalServer.post(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    internalServer.removeResource(getCurrentPath());
                    close();
                    return null;
                }
            });

        }

        return count;

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
