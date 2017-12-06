package com.namazustudios.socialengine.remote;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.remote.*;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

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

    private MockServiceInterface mockServiceInterface;

    @Test
    public void testSync() {

        setupMockToReturnFuture();
        getMockServiceInterface().testSyncVoid("Hello World!");

        final Invocation expected = new Invocation();

        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testSyncVoid");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));

        verify(getMockRemoteInvoker()).invoke(
            eq(expected),
            argThat((Consumer<InvocationError> ec) -> {
                ec.accept(new InvocationError());
                return true;
            }),
            eq(emptyList())
        );

    }

    @Test
    public void testDefaultMethod() {

        setupMockToReturnFuture();
        getMockServiceInterface().testDefaultMethod();

        final Invocation expected = new Invocation();

        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testSyncVoid");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));

        verify(getMockRemoteInvoker()).invoke(
                argThat(i -> i.equals(expected)),
                argThat((Consumer<InvocationError> ec) -> {
                    ec.accept(new InvocationError());
                    return true;
                }),
                eq(emptyList())
        );

    }


    @Test
    public void testSyncReturn() throws Exception {

        final Future<Object> future = setupMockToReturnFuture();
        when(future.get()).thenReturn(4.2);

        final double result = getMockServiceInterface().testSyncReturn("Hello World!");
        assertEquals(result, 4.2);
        verify(future, times(1)).get();

        final Invocation expected = new Invocation();

        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testSyncReturn");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));

        verify(getMockRemoteInvoker()).invoke(
            eq(expected),
            argThat((Consumer<InvocationError> ec) -> {
                ec.accept(new InvocationError());
                return true;
            }),
            eq(emptyList())
        );

    }

    @Test
    public void testAsyncVoid() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        getMockServiceInterface().testAsyncReturnVoid("Hello World!", resultHandler, throwableConsumer);
        verify(objectFuture, never()).get();

        final Invocation expected = new Invocation();
        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnVoid");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvoker()).invoke(
            argThat(i -> i.equals(expected)),
            argThat((Consumer<InvocationError> ec) -> {
                ec.accept(expectedInvocationError);
                return true;
            }),
            argThat(cl -> {
                cl.forEach(c -> c.accept(expectedInvocationResult));
                return true;
            })
        );

    }

    @Test
    public void testAsyncReturnFuture() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        when(objectFuture.get()).thenReturn(42);

        final Future<Integer> integerFuture = getMockServiceInterface().testAsyncReturnFuture("Hello World!");

        final int result = integerFuture.get();
        assertEquals(result, 42);
        verify(objectFuture).get();

        final Invocation expected = new Invocation();

        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));

        verify(getMockRemoteInvoker()).invoke(
            eq(expected),
            argThat((Consumer<InvocationError> ec) -> {
                ec.accept(new InvocationError());
                return true;
            }),
            eq(emptyList())
        );

    }

    @Test
    public void testAsyncReturnFutureWithConsumers() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        when(objectFuture.get()).thenReturn(42);

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        final Future<Integer> integerFuture = getMockServiceInterface().testAsyncReturnFuture("Hello World!", resultHandler, throwableConsumer);
        verify(objectFuture, never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        verify(objectFuture, times(1)).get();


        final Invocation expected = new Invocation();
        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvoker()).invoke(
                argThat(i -> i.equals(expected)),
                argThat((Consumer<InvocationError> ec) -> {
                    ec.accept(expectedInvocationError);
                    return true;
                }),
                argThat(cl -> {
                    cl.forEach(c -> c.accept(expectedInvocationResult));
                    return true;
                })
        );

    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumers() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        when(objectFuture.get()).thenReturn(42);

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final MockServiceInterface.MyStringHandler resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final MockServiceInterface.MyErrorHandler errorHandler = ex -> ex.equals(expectedRuntimeException);

        final Future<Integer> integerFuture = getMockServiceInterface().testAsyncReturnFuture("Hello World!", resultHandler, errorHandler);
        verify(objectFuture, never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        verify(objectFuture, times(1)).get();


        final Invocation expected = new Invocation();
        expected.setType(MockServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        verify(getMockRemoteInvoker()).invoke(
                argThat(i -> i.equals(expected)),
                argThat((Consumer<InvocationError> ec) -> {
                    ec.accept(expectedInvocationError);
                    return true;
                }),
                argThat(cl -> {
                    cl.forEach(c -> c.accept(expectedInvocationResult));
                    return true;
                })
        );

    }


    public Future<Object> setupMockToReturnFuture() {
        final Future<Object> future = mock(Future.class);
        when(getMockRemoteInvoker().invoke(any(), any(), any())).thenReturn(future);
        return future;
    }

    @AfterTest
    public void resetMocks() {
        reset(getMockRemoteInvoker());
    }

    public RemoteInvoker getMockRemoteInvoker() {
        return mockRemoteInvoker;
    }

    @Inject
    public void setMockRemoteInvoker(RemoteInvoker mockRemoteInvoker) {
        this.mockRemoteInvoker = mockRemoteInvoker;
    }

    public MockServiceInterface getMockServiceInterface() {
        return mockServiceInterface;
    }

    @Inject
    public void setMockServiceInterface(MockServiceInterface mockServiceInterface) {
        this.mockServiceInterface = mockServiceInterface;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {
            final RemoteInvoker remoteInvoker = mock(RemoteInvoker.class);
            bind(RemoteInvoker.class).toInstance(remoteInvoker);
            bind(MockServiceInterface.class).toProvider(new RemoteProxyProvider<>(MockServiceInterface.class));
        }

    }

}
