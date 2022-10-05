package com.namazustudios.socialengine.rt.transact;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.JsonRpcInvocationService;
import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.SimpleJsonRpcInvocationService;
import com.namazustudios.socialengine.rt.jackson.ObjectMapperPayloadReader;

public class SimpleJsonRpcInvocationServiceTestModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(PayloadReader.class)
            .to(ObjectMapperPayloadReader.class)
            .asEagerSingleton();

        bind(JsonRpcInvocationService.class)
            .to(SimpleJsonRpcInvocationService.class)
            .asEagerSingleton();

    }

}
