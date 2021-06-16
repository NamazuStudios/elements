package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.routing.BroadcastRoutingStrategy;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Guice(modules = {TestBroadcastRoutingStrategy.Module.class, RoutingTestModule.class})
public class TestBroadcastRoutingStrategy extends BaseRoutingStrategyTest {

    @Test
    public void testInvokeSync() throws Exception {

        final List<Object> address = emptyList();

        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
            (Consumer<InvocationResult>)mock(Consumer.class),
            (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        final List<RemoteInvoker> mockRemoteInvokers = unmodifiableList(asList(
            mock(RemoteInvoker.class),
            mock(RemoteInvoker.class),
            mock(RemoteInvoker.class),
            mock(RemoteInvoker.class),
            mock(RemoteInvoker.class)
        ));

        final List<InvocationResult> invocationResultList = new ArrayList<>();

        for(final RemoteInvoker ri : mockRemoteInvokers) {
            when(ri.invokeSync(eq(invocation), any(), any())).thenAnswer(i -> {
                final List<Consumer<InvocationResult>> arg1 = i.getArgument(1);
                arg1.forEach(c -> {
                    final InvocationResult ir = new InvocationResult();
                    c.accept(ir);
                    invocationResultList.add(ir);
                });
                return null;
            });
        }

        when(getRemoteInvokerRegistry()
            .getAllRemoteInvokers(eq(getApplicationId())))
            .thenReturn(mockRemoteInvokers);

        final Object result = getRoutingStrategy().invokeSync(
            address,
            invocation,
            asyncConsumers,
            invocationErrorConsumer);

        assertNull(result, "Expected null return.");
        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getApplicationId()));

        for (final RemoteInvoker ri : mockRemoteInvokers) {
            verify(ri, times(1)).invokeSync(
                eq(invocation),
                any(),
                any()
            );
        }

        assertEquals(invocationResultList.size(), mockRemoteInvokers.size() * asyncConsumers.size());
        invocationResultList.forEach(r -> assertNotNull(r));
        invocationResultList.forEach(r -> assertNull(r.getResult()));

    }

    @Test(expectedExceptions = {BullshitException.class})
    public void testInvokeSyncException() throws Exception {

        final List<Object> address = emptyList();
        final List<Consumer<InvocationResult>> asyncConsumers = new ArrayList<>();
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);

        when(getRemoteInvokerRegistry()
            .getAllRemoteInvokers(eq(getApplicationId())))
            .thenReturn(singletonList(mockRemoteInvoker));

        when(mockRemoteInvoker.invokeSync(any(), any(), any())).thenThrow(new BullshitException());
        getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

    }

    @Test
    public void testInvokeAsync() {

        final List<Object> address = emptyList();

        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        final List<RemoteInvoker> mockRemoteInvokers = unmodifiableList(asList(
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class)
        ));

        final List<InvocationResult> invocationResultList = new ArrayList<>();

        for(final RemoteInvoker ri : mockRemoteInvokers) {
            when(ri.invokeAsyncV(eq(invocation), any(), any())).thenAnswer(i -> {
                final List<Consumer<InvocationResult>> arg1 = i.getArgument(1);
                arg1.forEach(c -> {
                    final InvocationResult ir = new InvocationResult();
                    c.accept(ir);
                    invocationResultList.add(ir);
                });
                return null;
            });
        }

        when(getRemoteInvokerRegistry()
                .getAllRemoteInvokers(eq(getApplicationId())))
                .thenReturn(mockRemoteInvokers);

        final Object result = getRoutingStrategy().invokeAsyncV(
                address,
                invocation,
                asyncConsumers,
                invocationErrorConsumer);

        assertNull(result, "Expected null return.");
        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getApplicationId()));

        for (final RemoteInvoker ri : mockRemoteInvokers) {
            verify(ri, times(1)).invokeAsyncV(
                    eq(invocation),
                    any(),
                    any()
            );
        }

        assertEquals(invocationResultList.size(), mockRemoteInvokers.size() * asyncConsumers.size());
        invocationResultList.forEach(r -> assertNotNull(r));
        invocationResultList.forEach(r -> assertNull(r.getResult()));

    }

    @Test
    public void testInvokeFuture() throws ExecutionException, InterruptedException {
        final List<Object> address = emptyList();

        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final List<Consumer<InvocationResult>> asyncConsumers = unmodifiableList(asList(
                (Consumer<InvocationResult>)mock(Consumer.class),
                (Consumer<InvocationResult>)mock(Consumer.class)
        ));

        final List<RemoteInvoker> mockRemoteInvokers = unmodifiableList(asList(
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class),
                mock(RemoteInvoker.class)
        ));

        final List<InvocationResult> invocationResultList = new ArrayList<>();

        for(final RemoteInvoker ri : mockRemoteInvokers) {
            when(ri.invokeCompletionStage(eq(invocation), any(), any())).thenAnswer(i -> {
                final List<Consumer<InvocationResult>> arg1 = i.getArgument(1);
                arg1.forEach(c -> {
                    final InvocationResult ir = new InvocationResult();
                    c.accept(ir);
                    invocationResultList.add(ir);
                });

                final CompletableFuture<Object> future = new CompletableFuture<>();
                future.complete(null);
                return future;

            });
        }

        when(getRemoteInvokerRegistry()
                .getAllRemoteInvokers(eq(getApplicationId())))
                .thenReturn(mockRemoteInvokers);

        final Future<Object> result = getRoutingStrategy().invokeFuture(
                address,
                invocation,
                asyncConsumers,
                invocationErrorConsumer);

        assertNotNull(result, "Expected null return.");
        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getApplicationId()));

        for (final RemoteInvoker ri : mockRemoteInvokers) {
            verify(ri, times(1)).invokeCompletionStage(
                    eq(invocation),
                    any(),
                    any()
            );
        }

        assertEquals(invocationResultList.size(), mockRemoteInvokers.size() * asyncConsumers.size());
        invocationResultList.forEach(r -> assertNotNull(r));
        invocationResultList.forEach(r -> assertNull(r.getResult()));
        assertNull(result.get());

    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(RoutingStrategy.class).to(BroadcastRoutingStrategy.class);
        }

    }

}
