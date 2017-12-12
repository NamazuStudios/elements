package com.namazustudios.socialengine.remote;

import com.namazustudios.socialengine.rt.annotation.*;

import java.util.concurrent.Future;
import java.util.function.Consumer;

@Proxyable
public interface TestServiceInterface {

    @RemotelyInvokable
    void testSyncVoid(@Serialize String msg);

    @RemotelyInvokable
    double testSyncReturn(@Serialize String msg);

    @RemotelyInvokable
    void testAsyncReturnVoid(@Serialize String msg,
                             @ResultHandler Consumer<String> stringConsumer,
                             @ErrorHandler Consumer<Throwable> throwableConsumer);

    @RemotelyInvokable
    Future<Integer> testAsyncReturnFuture(@Serialize String msg);

    @RemotelyInvokable
    Future<Integer> testAsyncReturnFuture(@Serialize String msg,
                                          @ResultHandler Consumer<String> stringConsumer,
                                          @ErrorHandler Consumer<Throwable> throwableConsumer);

    @RemotelyInvokable
    Future<Integer> testAsyncReturnFuture(@Serialize String msg,
                                          @ResultHandler MyStringHandler stringConsumer,
                                          @ErrorHandler MyErrorHandler errorHandler);


    default void testDefaultMethod() {
        testSyncVoid("Hello World!");
    }

    @FunctionalInterface
    interface MyErrorHandler { void handle(Throwable throwable); }

    @FunctionalInterface
    interface MyStringHandler { void handle(Object result); }

}
