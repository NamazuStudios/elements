package dev.getelements.elements.rt.remote;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.rt.annotation.Dispatch;
import dev.getelements.elements.rt.routing.DefaultRoutingStrategy;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@Guice(modules = RemoteProxyProviderUnitTest.Module.class)
public class RemoteProxyProviderUnitTest {

    private RemoteInvocationDispatcher mockRemoteInvocationDispatcher;

    private TestServiceInterface testServiceInterface;

    @BeforeMethod
    public void resetMocks() {
        reset(getMockRemoteInvocationDispatcher());
    }

    @Test
    public void testSync() throws Exception {

        when(getMockRemoteInvocationDispatcher().invokeSync(any(), any(), any(), any()))
            .thenReturn(null);

        getTestServiceInterface().testSyncVoid("Hello World!");

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testSyncVoid");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));
        invocation.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        verify(getMockRemoteInvocationDispatcher()).invokeSync(
            eq(route),
            eq(invocation),
            eq(emptyList()),
            any(InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testDefaultMethod() throws Exception {

        when(getMockRemoteInvocationDispatcher().invokeSync(any(), any(), any(), any())).thenReturn(null);

        getTestServiceInterface().testDefaultMethod();

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testSyncVoid");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));
        invocation.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        verify(getMockRemoteInvocationDispatcher()).invokeSync(
            eq(route),
            argThat(i -> i.equals(invocation)),
            eq(emptyList()),
            any(InvocationErrorConsumer.class)
        );

    }


    @Test
    public void testSyncReturn() throws Exception {

        when(getMockRemoteInvocationDispatcher().invokeSync(any(), any(), any(), any())).thenReturn(4.2);

        final double result = getTestServiceInterface().testSyncReturn("Hello World!");
        assertEquals(result, 4.2);

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testSyncReturn");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));
        invocation.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        verify(getMockRemoteInvocationDispatcher()).invokeSync(eq(route), eq(invocation), eq(emptyList()), any());

    }

    @Test
    public void testAsyncVoid() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        getTestServiceInterface().testAsyncReturnVoid("Hello World!", resultHandler, throwableConsumer);
        verify(objectFuture, Mockito.never()).get();

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation invocation = new Invocation();
        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnVoid");
        invocation.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        invocation.setArguments(asList("Hello World!"));
        invocation.setDispatchType(Dispatch.Type.ASYNCHRONOUS);

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvocationDispatcher()).invokeAsyncV(
            eq(route),
            argThat(i -> i.equals(invocation)),
            argThat(cl -> {
                cl.forEach(c -> c.accept(expectedInvocationResult));
                return true;
            }),
            argThat((InvocationErrorConsumer ec) -> {
                try {
                    ec.accept(new InvocationError());
                    return true;
                } catch (Throwable throwable) {
                    return false;
                }
            })
        );

    }

    @Test
    public void testAsyncReturnFuture() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        when(objectFuture.get()).thenReturn(42);

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello World!");

        final int result = integerFuture.get();
        assertEquals(result, 42);
        verify(objectFuture).get();

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnFuture");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));
        invocation.setDispatchType(Dispatch.Type.FUTURE);

        verify(getMockRemoteInvocationDispatcher()).invokeFuture(
            eq(route),
            eq(invocation),
            eq(emptyList()), any(InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testAsyncReturnFutureWithConsumers() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        when(objectFuture.get()).thenReturn(42);

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello World!", resultHandler, throwableConsumer);
        verify(objectFuture, Mockito.never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        verify(objectFuture, Mockito.times(1)).get();

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.FUTURE);

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvocationDispatcher()).invokeFuture(
            eq(route),
            argThat(i -> i.equals(expected)),
            argThat(cl -> {
                cl.forEach(c -> c.accept(expectedInvocationResult));
                return true;
            }), any(InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumers() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        when(objectFuture.get()).thenReturn(42);

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final TestServiceInterface.MyStringHandler resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final TestServiceInterface.MyErrorHandler errorHandler = ex -> ex.equals(expectedRuntimeException);

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello World!", resultHandler, errorHandler);
        verify(objectFuture, Mockito.never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        verify(objectFuture, Mockito.times(1)).get();

        final Route route = new Route();
        route.setAddress(emptyList());
        route.setRoutingStrategyType(DefaultRoutingStrategy.class);

        final Invocation invocation = new Invocation();
        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnFuture");
        invocation.setParameters(asList(String.class.getName(), TestServiceInterface.MyStringHandler.class.getName(), TestServiceInterface.MyErrorHandler.class.getName()));
        invocation.setArguments(asList("Hello World!"));
        invocation.setDispatchType(Dispatch.Type.FUTURE);

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvocationDispatcher()).invokeFuture(
            eq(route),
            argThat(i -> i.equals(invocation)),
            argThat(cl -> {
                cl.forEach(c -> c.accept(expectedInvocationResult));
                return true;
            }), argThat((InvocationErrorConsumer ec) -> {
                try {
                    ec.accept(new InvocationError());
                    return true;
                } catch (Throwable throwable) {
                    return false;
                }
            })
        );

    }


    public Future<Object> setupMockToReturnFuture() {
        final Future<Object> future = mock(Future.class);
        when(getMockRemoteInvocationDispatcher().invokeFuture(any(), any(), any(), any())).thenReturn(future);
        return future;
    }

    public RemoteInvocationDispatcher getMockRemoteInvocationDispatcher() {
        return mockRemoteInvocationDispatcher;
    }

    @Inject
    public void setMockRemoteInvocationDispatcher(RemoteInvocationDispatcher mockRemoteInvocationDispatcher) {
        this.mockRemoteInvocationDispatcher = mockRemoteInvocationDispatcher;
    }

    public TestServiceInterface getTestServiceInterface() {
        return testServiceInterface;
    }

    @Inject
    public void setTestServiceInterface(TestServiceInterface testServiceInterface) {
        this.testServiceInterface = testServiceInterface;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            final RemoteInvocationDispatcher remoteInvocationDispatcher = mock(RemoteInvocationDispatcher.class);
            bind(RemoteInvocationDispatcher.class).toInstance(remoteInvocationDispatcher);
            bind(TestServiceInterface.class).toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));
        }

    }

}
