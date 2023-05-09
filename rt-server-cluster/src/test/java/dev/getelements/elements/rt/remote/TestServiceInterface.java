package dev.getelements.elements.rt.remote;

import dev.getelements.elements.rt.annotation.*;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.annotation.RemoteScope.*;

@Proxyable
@RemoteService(scopes = {
        @RemoteScope(scope = MASTER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL),
        @RemoteScope(scope = WORKER_SCOPE, protocol = ELEMENTS_RT_PROTOCOL)
})
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

    @FunctionalInterface
    interface MyErrorHandler { void handle(Throwable throwable); }

    @FunctionalInterface
    interface MyStringHandler { void handle(Object result); }

    @RemotelyInvokable
    default void testDefaultMethod() {
        testSyncVoid("Hello World!");
    }

    @RemotelyInvokable
    String testEcho(@Serialize String msg, @Serialize double errorChance);

}
