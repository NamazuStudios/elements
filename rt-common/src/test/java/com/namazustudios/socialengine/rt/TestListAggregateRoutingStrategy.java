package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.routing.ListAggregateRoutingStrategy;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Guice(modules = {TestListAggregateRoutingStrategy.Module.class, RoutingTestModule.class})
public class TestListAggregateRoutingStrategy extends BaseRoutingStrategyTest {

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

        final List<List<InvocationResult>> mockResults = asyncConsumers
            .stream()
            .map(c -> mockRemoteInvokers
                .stream()
                .map(i -> new InvocationResult(singletonList(randomUUID())))
                .collect(toList())
            ).collect(toList());

        final List<UUID> expectedAggregateList = new ArrayList<>();

        for (int j = 0; j < mockRemoteInvokers.size(); ++j) {

            final int remoteInvokerIndex = j;
            final RemoteInvoker ri = mockRemoteInvokers.get(j);

            when(ri.invokeSync(eq(invocation), any(), any())).thenAnswer(inv -> {

                final AtomicInteger consumerIndex = new AtomicInteger();
                final List<Consumer<InvocationResult>> arg1 = inv.getArgument(1);

                arg1.forEach(c -> {
                    final InvocationResult ir = mockResults
                            .get(consumerIndex.getAndIncrement())
                            .get(remoteInvokerIndex);
                    c.accept(ir);
                });

                final UUID uuid = randomUUID();
                expectedAggregateList.add(uuid);
                return singletonList(uuid);

            });

        }

        when(getRemoteInvokerRegistry()
            .getAllRemoteInvokers(eq(getDefaultApplicationUuid())))
            .thenReturn(mockRemoteInvokers);

        final Object result = getRoutingStrategy().invokeSync(
            address,
            invocation,
            asyncConsumers,
            invocationErrorConsumer);

        assertNotNull(result, "Expected non-null return.");
        assertTrue(result instanceof List);

        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getDefaultApplicationUuid()));

        for (final RemoteInvoker ri : mockRemoteInvokers) {
            verify(ri, times(1)).invokeSync(
                eq(invocation),
                any(),
                any()
            );
        }

        final List<UUID> resultList = checkedList((List)result, UUID.class);
        assertEquals(resultList, expectedAggregateList);

        final List<InvocationResult> expectedResults = mockResults
                .stream()
                .map(lir -> lir.stream().flatMap(ir -> ((List<Object>)ir.getResult()).stream()).collect(toList()))
                .map(InvocationResult::new)
                .collect(toList());

        for (int i = 0; i < expectedResults.size(); ++i) {
            final InvocationResult expected = expectedResults.get(i);
            final Consumer<InvocationResult> consumer = asyncConsumers.get(i);
            verify(consumer, times(1)).accept(eq(expected));
        }

    }

    @Test(expectedExceptions = {BullshitException.class})
    public void testInvokeSyncException() throws Exception {

        final List<Object> address = emptyList();
        final List<Consumer<InvocationResult>> asyncConsumers = new ArrayList<>();
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        final RemoteInvoker mockRemoteInvoker = mock(RemoteInvoker.class);

        when(getRemoteInvokerRegistry()
            .getAllRemoteInvokers(eq(getDefaultApplicationUuid())))
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

        final List<List<InvocationResult>> mockResults = asyncConsumers
            .stream()
            .map(c -> mockRemoteInvokers
                .stream()
                .map(i -> new InvocationResult(singletonList(randomUUID())))
                .collect(toList())
            ).collect(toList());

        for (int j = 0; j < mockRemoteInvokers.size(); ++j) {

            final int remoteInvokerIndex = j;
            final RemoteInvoker ri = mockRemoteInvokers.get(j);

            when(ri.invokeAsync(eq(invocation), any(), any())).thenAnswer(inv -> {

                final AtomicInteger consumerIndex = new AtomicInteger();
                final List<Consumer<InvocationResult>> arg1 = inv.getArgument(1);

                arg1.forEach(c -> {
                    final InvocationResult ir = mockResults
                        .get(consumerIndex.getAndIncrement())
                        .get(remoteInvokerIndex);
                    c.accept(ir);
                });

                return null;

            });

        }

        when(getRemoteInvokerRegistry()
                .getAllRemoteInvokers(eq(getDefaultApplicationUuid())))
                .thenReturn(mockRemoteInvokers);

        final Object result = getRoutingStrategy().invokeAsync(
                address,
                invocation,
                asyncConsumers,
                invocationErrorConsumer);

        assertNull(result, "Expected null return.");
        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getDefaultApplicationUuid()));

        for (final RemoteInvoker ri : mockRemoteInvokers) {
            verify(ri, times(1)).invokeAsync(
                    eq(invocation),
                    any(),
                    any()
            );
        }

        final List<InvocationResult> expectedResults = mockResults
            .stream()
            .map(lir -> lir.stream().flatMap(ir -> ((List<Object>)ir.getResult()).stream()).collect(toList()))
            .map(InvocationResult::new)
            .collect(toList());

        for (int i = 0; i < expectedResults.size(); ++i) {
            final InvocationResult expected = expectedResults.get(i);
            final Consumer<InvocationResult> consumer = asyncConsumers.get(i);
            verify(consumer, times(1)).accept(eq(expected));
        }

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

        final List<List<InvocationResult>> mockResults = asyncConsumers
            .stream()
            .map(c -> mockRemoteInvokers
                .stream()
                .map(i -> new InvocationResult(singletonList(randomUUID())))
                .collect(toList())
            ).collect(toList());

        final List<UUID> expectedAggregateList = new ArrayList<>();

        for (int j = 0; j < mockRemoteInvokers.size(); ++j) {

            final int remoteInvokerIndex = j;
            final RemoteInvoker ri = mockRemoteInvokers.get(j);

            when(ri.invokeCompletionStage(eq(invocation), any(), any())).thenAnswer(inv -> {

                final AtomicInteger consumerIndex = new AtomicInteger();
                final List<Consumer<InvocationResult>> arg1 = inv.getArgument(1);

                arg1.forEach(c -> {
                    final InvocationResult ir = mockResults
                            .get(consumerIndex.getAndIncrement())
                            .get(remoteInvokerIndex);
                    c.accept(ir);
                });

                final UUID uuid = randomUUID();
                expectedAggregateList.add(uuid);

                final CompletableFuture<Object> future = new CompletableFuture<>();
                future.complete(singletonList(uuid));

                return future;

            });

        }

        when(getRemoteInvokerRegistry()
            .getAllRemoteInvokers(eq(getDefaultApplicationUuid())))
            .thenReturn(mockRemoteInvokers);

        final Future<Object> result = getRoutingStrategy().invokeFuture(
            address,
            invocation,
            asyncConsumers,
            invocationErrorConsumer);

        assertNotNull(result, "Expected non-null return.");
        assertTrue(result.get() instanceof List);

        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getDefaultApplicationUuid()));

        for (final RemoteInvoker ri : mockRemoteInvokers) {
            verify(ri, times(1)).invokeCompletionStage(
                eq(invocation),
                any(),
                any()
            );
        }

        final List<UUID> resultList = checkedList((List)result.get(), UUID.class);
        assertEquals(resultList, expectedAggregateList);
        assertEquals(resultList, expectedAggregateList);

        final List<InvocationResult> expectedResults = mockResults
            .stream()
            .map(lir -> lir.stream().flatMap(ir -> ((List<Object>)ir.getResult()).stream()).collect(toList()))
            .map(InvocationResult::new)
            .collect(toList());

        for (int i = 0; i < expectedResults.size(); ++i) {
            final InvocationResult expected = expectedResults.get(i);
            final Consumer<InvocationResult> consumer = asyncConsumers.get(i);
            verify(consumer, times(1)).accept(eq(expected));
        }

    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(RoutingStrategy.class).to(ListAggregateRoutingStrategy.class);
        }

    }


}
