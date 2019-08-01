package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.routing.BroadcastRoutingStrategy;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
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
            .getAllRemoteInvokers(eq(getDefaultApplicationUuid())))
            .thenReturn(mockRemoteInvokers);

        final Object result = getRoutingStrategy().invokeSync(
            address,
            invocation,
            asyncConsumers,
            invocationErrorConsumer);

        assertNull(result, "Expected null return.");
        verify(getRemoteInvokerRegistry(), times(1)).getAllRemoteInvokers(eq(getDefaultApplicationUuid()));

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

    @Test
    public void testInvokeSyncException() throws Exception {

        final List<Object> address = spy(emptyList());
        final List<Consumer<InvocationResult>> asyncConsumers = new ArrayList<>();
        final InvocationErrorConsumer invocationErrorConsumer = mock(InvocationErrorConsumer.class);
        final Invocation invocation = spy(Invocation.class);

        getRoutingStrategy().invokeSync(address, invocation, asyncConsumers, invocationErrorConsumer);

    }

    @Test
    public void testInvokeAsync() {

    }

    @Test
    public void testInvokeFuture() {

    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            bind(RoutingStrategy.class).to(BroadcastRoutingStrategy.class);
        }

    }

}
