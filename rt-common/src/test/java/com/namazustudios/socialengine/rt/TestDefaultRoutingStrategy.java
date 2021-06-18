package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.routing.DefaultRoutingStrategy;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Guice(modules = {RoutingTestModule.class, TestDefaultRoutingStrategy.Module.class})
public class TestDefaultRoutingStrategy extends BaseRoutingStrategyTest {

    @Test
    public void testInvokeSync() throws Exception {

        final List<Object> address = emptyList();
        final Object mockResult = mock(Object.class);
        final Invocation invocation = spy(Invocation.class);
        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
            (Consumer<InvocationResult>)mock(Consumer.class),
            (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        when(getRemoteInvokerRegistry()
            .getBestRemoteInvoker(getApplicationId()))
            .thenReturn(mockRemoteInvoker);

        when(mockRemoteInvoker.invokeSync(invocation, asyncConsumers, invocationErrorConsumer))
            .thenReturn(mockResult);

        final Object result =
            getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

        verify(mockRemoteInvoker, times(1))
           .invokeSync(eq(invocation), eq(asyncConsumers), eq(invocationErrorConsumer));

        assertEquals(result, mockResult);

    }

    @Test(expectedExceptions = {BullshitException.class})
    public void testInvokeSyncException() throws Exception {

        final List<Object> address = emptyList();
        final List<Consumer<InvocationResult>> asyncConsumers = new ArrayList<>();
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);

        when(getRemoteInvokerRegistry()
            .getBestRemoteInvoker(eq(getApplicationId())))
            .thenReturn(mockRemoteInvoker);

        when(mockRemoteInvoker.invokeSync(any(), any(), any())).thenThrow(new BullshitException());
        getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

    }

    @Test
    public void testInvokeAsync() {

        final List<Object> address = emptyList();
        final Invocation invocation = spy(Invocation.class);
        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        when(getRemoteInvokerRegistry()
            .getBestRemoteInvoker(getApplicationId()))
            .thenReturn(mockRemoteInvoker);

        doNothing()
            .when(mockRemoteInvoker)
            .invokeAsyncV(invocation, asyncConsumers, invocationErrorConsumer);

        final Void result = getRoutingStrategy()
            .invokeAsyncV(address, invocation, asyncConsumers, invocationErrorConsumer);

        assertNull(result);

        verify(mockRemoteInvoker, times(1))
            .invokeAsync(eq(invocation), eq(asyncConsumers), eq(invocationErrorConsumer));

    }

    @Test
    public void testInvokeFuture() throws ExecutionException, InterruptedException {

        final List<Object> address = emptyList();
        final Invocation invocation = spy(Invocation.class);
        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);

        final Object mockResult = mock(Object.class);
        final Future<Object> mockResultFuture = mock(Future.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        when(getRemoteInvokerRegistry()
                .getBestRemoteInvoker(getApplicationId()))
                .thenReturn(mockRemoteInvoker);

        when(mockRemoteInvoker.invokeFuture(invocation, asyncConsumers, invocationErrorConsumer))
                .thenReturn(mockResultFuture);

        when(mockResultFuture.get()).thenReturn(mockResult);

        final Future<Object> resultFuture =
            getRoutingStrategy().invokeFuture(address, invocation, asyncConsumers, invocationErrorConsumer);

        verify(mockRemoteInvoker, times(1))
            .invokeFuture(eq(invocation), eq(asyncConsumers), eq(invocationErrorConsumer));

        assertEquals(resultFuture, mockResultFuture);
        assertEquals(resultFuture.get(), mockResult);

    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(RoutingStrategy.class).to(DefaultRoutingStrategy.class);
        }

    }


}
