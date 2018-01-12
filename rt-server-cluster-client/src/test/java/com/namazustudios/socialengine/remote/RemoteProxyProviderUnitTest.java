package com.namazustudios.socialengine.remote;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.remote.InvocationError;
import com.namazustudios.socialengine.rt.remote.InvocationResult;
import com.namazustudios.socialengine.rt.annotation.Dispatch;
import com.namazustudios.socialengine.rt.remote.*;
import org.testng.annotations.*;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

@Guice(modules = RemoteProxyProviderUnitTest.Module.class)
public class RemoteProxyProviderUnitTest {

    private RemoteInvoker mockRemoteInvoker;

    private TestServiceInterface testServiceInterface;

        @AfterMethod
        public void resetMocks() {
            reset(getMockRemoteInvoker());
        }

    @Test
    public void testSync() throws Exception {

        final Future<Object> objectFuture =  setupMockToReturnFuture();
        getTestServiceInterface().testSyncVoid("Hello World!");
        verify(objectFuture, times(1)).get();

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testSyncVoid");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        verify(getMockRemoteInvoker()).invoke(
            eq(expected),
                eq(emptyList()), any(RemoteInvoker.InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testDefaultMethod() throws Exception {

        final Future<Object> objectFuture =  setupMockToReturnFuture();
        getTestServiceInterface().testDefaultMethod();
        verify(objectFuture, times(1)).get();

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testDefaultMethod");
        expected.setParameters(emptyList());
        expected.setArguments(emptyList());
        expected.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        verify(getMockRemoteInvoker()).invoke(
                argThat(i -> i.equals(expected)),
                eq(emptyList()), any(RemoteInvoker.InvocationErrorConsumer.class)
        );

    }


    @Test
    public void testSyncReturn() throws Exception {

        final Future<Object> future = setupMockToReturnFuture();
        when(future.get()).thenReturn(4.2);

        final double result = getTestServiceInterface().testSyncReturn("Hello World!");
        assertEquals(result, 4.2);
        verify(future, times(1)).get();

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testSyncReturn");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        verify(getMockRemoteInvoker()).invoke(eq(expected), eq(emptyList()), any()
        );

    }

    @Test
    public void testAsyncVoid() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        getTestServiceInterface().testAsyncReturnVoid("Hello World!", resultHandler, throwableConsumer);
        verify(objectFuture, never()).get();

        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnVoid");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.CONSUMER);

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvoker()).invoke(
            argThat(i -> i.equals(expected)),
                argThat(cl -> {
                    cl.forEach(c -> c.accept(expectedInvocationResult));
                    return true;
                }), argThat((RemoteInvoker.InvocationErrorConsumer ec) -> {
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

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.FUTURE);

        verify(getMockRemoteInvoker()).invoke(
            eq(expected),
                eq(emptyList()), any(RemoteInvoker.InvocationErrorConsumer.class)
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
        verify(objectFuture, never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        verify(objectFuture, times(1)).get();


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

        verify(getMockRemoteInvoker()).invoke(
                argThat(i -> i.equals(expected)),
                argThat(cl -> {
                    cl.forEach(c -> c.accept(expectedInvocationResult));
                    return true;
                }), any(RemoteInvoker.InvocationErrorConsumer.class)
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
        verify(objectFuture, never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        verify(objectFuture, times(1)).get();


        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName(), TestServiceInterface.MyStringHandler.class.getName(), TestServiceInterface.MyErrorHandler.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.FUTURE);

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvoker()).invoke(
                argThat(i -> i.equals(expected)),
                argThat(cl -> {
                    cl.forEach(c -> c.accept(expectedInvocationResult));
                    return true;
                }), argThat((RemoteInvoker.InvocationErrorConsumer ec) -> {
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
        when(getMockRemoteInvoker().invoke(any(), any(), any())).thenReturn(future);
        return future;
    }

    public RemoteInvoker getMockRemoteInvoker() {
        return mockRemoteInvoker;
    }

    @Inject
    public void setMockRemoteInvoker(RemoteInvoker mockRemoteInvoker) {
        this.mockRemoteInvoker = mockRemoteInvoker;
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
            final RemoteInvoker remoteInvoker = mock(RemoteInvoker.class);
            bind(RemoteInvoker.class).toInstance(remoteInvoker);
            bind(TestServiceInterface.class).toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));
        }

    }

}
