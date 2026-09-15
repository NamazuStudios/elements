package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;

public interface RemoteInvocationDispatcher {

    void dispatch(Invocation invocation, ResultAsyncHandler resultHandler);

    interface ResultAsyncHandler {

        void onInvocationError(InvocationError error);

        void onInvocationResult(InvocationResult result);

    }

}
