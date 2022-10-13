package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.jrpc.JsonRpcInvocationService;
import com.namazustudios.socialengine.rt.SimpleJsonRpcInvocationService;

public class SimpleJsonRpcInvocationServiceTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JsonRpcInvocationService.class)
            .to(SimpleJsonRpcInvocationService.class)
            .asEagerSingleton();
    }

}
