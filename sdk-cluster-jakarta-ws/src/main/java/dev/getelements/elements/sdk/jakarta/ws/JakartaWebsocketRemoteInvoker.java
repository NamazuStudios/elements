package dev.getelements.elements.sdk.jakarta.ws;

import dev.getelements.elements.sdk.cluster.remote.*;
import jakarta.websocket.ClientEndpoint;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ClientEndpoint
public class JakartaWebsocketRemoteInvoker implements RemoteInvoker {

    @Override
    public String getConnectAddress() {
        return "";
    }

    @Override
    public void start(String connectAddress, long timeout, TimeUnit timeoutTimeUnit) {

    }

    @Override
    public AsyncOperation invokeAsync(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return null;
    }

    @Override
    public CompletionStage<Object> invokeCompletionStage(Invocation invocation, List<Consumer<InvocationResult>> asyncInvocationResultConsumerList, InvocationErrorConsumer asyncInvocationErrorConsumer) {
        return null;
    }

}
