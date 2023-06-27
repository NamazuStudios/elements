package dev.getelements.elements.rt;

import com.google.inject.AbstractModule;
import dev.getelements.elements.rt.jrpc.JsonRpcInvocationService;
import dev.getelements.elements.rt.SimpleJsonRpcInvocationService;

public class SimpleJsonRpcInvocationServiceTestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(JsonRpcInvocationService.class)
            .to(SimpleJsonRpcInvocationService.class)
            .asEagerSingleton();
    }

}
