package com.namazustudios.socialengine.rt.lua;

import com.google.common.reflect.TypeResolver;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.internal.InternalRequestPathHandler;
import com.namazustudios.socialengine.rt.internal.InternalResource;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 8/27/15.
 */
public class LuaInternalResource extends AbstractLuaResource implements InternalResource {

    @Inject
    public LuaInternalResource(final LuaState luaState,
                               final IocResolver iocResolver,
                               final TypeRegistry typeRegistry) {
        super(luaState, iocResolver, typeRegistry);
    }

    @Override
    public InternalRequestPathHandler getHandler(final String method) {
        return new InternalRequestPathHandler() {

            @Override
            public Class<?> getPayloadType() {
                return getRequestType(method);
            }

            @Override
            public void handle(final Request request, final ResponseReceiver responseReceiver) {

            }

        };
    }

}
