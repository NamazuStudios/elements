//package com.namazustudios.socialengine.rt.lua;
//
//import com.naef.jnlua.LuaState;
//import com.namazustudios.socialengine.rt.Request;
//import com.namazustudios.socialengine.rt.ResponseReceiver;
//import com.namazustudios.socialengine.rt.Scheduler;
//import com.namazustudios.socialengine.rt.SimpleResponse;
//import com.namazustudios.socialengine.rt.handler.ClientRequestHandler;
//import com.namazustudios.socialengine.rt.handler.Session;
//import com.namazustudios.socialengine.rt.handler.Handler;
//
//import javax.inject.Inject;
//import java.util.Map;
//
///**
// * Created by patricktwohig on 8/27/15.
// */
//public class LuaHandler extends AbstractLuaResource implements Handler {
//
//    private final LuaState luaState;
//
//    private final Tabler tabler;
//
//    @Inject
//    public LuaHandler(final LuaState luaState,
//                      final IocResolver iocResolver,
//                      final Tabler tabler,
//                      final Scheduler<Handler> edgeContainer) {
//        super(luaState, iocResolver, tabler, edgeContainer);
//        this.luaState = luaState;
//        this.tabler = tabler;
//    }
//
//    @Override
//    public ClientRequestHandler getHandler(final String method) {
//        return new ClientRequestHandler() {
//
//            @Override
//            public Class<?> getPayloadType() {
//                return Map.class;
//            }
//
//            @Override
//            public void handle(final Session session,
//                               final Request request,
//                               final ResponseReceiver responseReceiver) {
//                try (final StackProtector stackProtector = new StackProtector(luaState)){
//
//                    pushRequestHandlerFunction(method);
//
//                    luaState.pushJavaObject(session);
//                    luaState.pushJavaObject(request.getHeader());
//
//                    final Map requestPayload = request.getPayload(Map.class);
//
//                    if (requestPayload == null) {
//                        luaState.pushNil();
//                    } else {
//                        tabler.push(luaState, request.getPayload(Map.class));
//                    }
//
//                    luaState.call(3, 2);
//
//                    final SimpleResponse simpleResponse = SimpleResponse.builder()
//                            .from(request)
//                            .code((int)luaState.checkNumber(-2))
//                            .payload(luaState.checkJavaObject(-1, Object.class))
//                        .build();
//
//                    responseReceiver.receive(simpleResponse);
//
//                }
//            }
//
//        };
//    }
//
//}
