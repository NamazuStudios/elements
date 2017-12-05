package com.namazustudios.socialengine.remote;

import com.namazustudios.socialengine.rt.annotation.*;

import java.util.concurrent.Future;
import java.util.function.Consumer;

@Proxyable
public interface MockServiceInterface {

    @RemotelyInvokable
    void testSyncVoid(@Serialize String msg);

    @RemotelyInvokable
    double testSyncReturn(@Serialize String msg);

    @RemotelyInvokable
    Future<Integer> testAsyncReturnFuture(@Serialize String msg);

    @RemotelyInvokable
    Future<Integer> testAsync(@Serialize String msg);

    @RemotelyInvokable
    Future<Integer> testAsync(@Serialize String msg,
                              @ResultHandler Consumer<String> stringConsumer,
                              @ErrorHandler Consumer<Throwable> throwableConsumer);

}
