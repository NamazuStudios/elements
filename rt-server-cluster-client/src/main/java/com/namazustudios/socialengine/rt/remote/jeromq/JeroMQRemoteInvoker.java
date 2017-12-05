package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.remote.InvocationError;
import com.namazustudios.socialengine.rt.remote.InvocationResult;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;

import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    @Override
    public Future<Object> invoke(final Invocation invocation,
                                 final Consumer<InvocationError> errorInvocationResultConsumer,
                                 final List<Consumer<InvocationResult>> invocationResultConsumerList) {
        return null;
    }

}
