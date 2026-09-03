package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;

import java.util.function.Consumer;

public interface RemoteInvocationDispatcher {

    void dispatch(Invocation invocation, Consumer<Object> resultConsumer);

}
