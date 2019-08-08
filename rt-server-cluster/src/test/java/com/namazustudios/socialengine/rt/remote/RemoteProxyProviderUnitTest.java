package com.namazustudios.socialengine.rt.remote;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.annotation.Dispatch;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testng.annotations.*;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;

@Guice(modules = RemoteProxyProviderUnitTest.Module.class)
public class RemoteProxyProviderUnitTest {

    private RemoteInvoker mockRemoteInvoker;

    private TestServiceInterface testServiceInterface;

    @BeforeMethod
    public void resetMocks() {
        Mockito.reset(getMockRemoteInvoker());
    }

    @Test
    public void testSync() throws Exception {

        Mockito.when(mockRemoteInvoker.invokeSync(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);

        getTestServiceInterface().testSyncVoid("Hello World!");

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testSyncVoid");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        Mockito.verify(getMockRemoteInvoker()).invokeSync(
            ArgumentMatchers.eq(expected),
            ArgumentMatchers.eq(emptyList()),
            ArgumentMatchers.any(InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testDefaultMethod() throws Exception {

        Mockito.when(mockRemoteInvoker.invokeSync(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(null);

        getTestServiceInterface().testDefaultMethod();

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testDefaultMethod");
        expected.setParameters(emptyList());
        expected.setArguments(emptyList());
        expected.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        Mockito.verify(getMockRemoteInvoker()).invokeSync(
            ArgumentMatchers.argThat(i -> i.equals(expected)),
            ArgumentMatchers.eq(emptyList()), ArgumentMatchers.any(InvocationErrorConsumer.class)
        );

    }


    @Test
    public void testSyncReturn() throws Exception {

        Mockito.when(mockRemoteInvoker.invokeSync(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(4.2);

        final double result = getTestServiceInterface().testSyncReturn("Hello World!");
        assertEquals(result, 4.2);

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testSyncReturn");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.SYNCHRONOUS);

        Mockito.verify(getMockRemoteInvoker()).invokeSync(ArgumentMatchers.eq(expected), ArgumentMatchers.eq(emptyList()), ArgumentMatchers.any());

    }

    @Test
    public void testAsyncVoid() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        getTestServiceInterface().testAsyncReturnVoid("Hello World!", resultHandler, throwableConsumer);
        Mockito.verify(objectFuture, Mockito.never()).get();

        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnVoid");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.ASYNCHRONOUS);

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        Mockito.verify(getMockRemoteInvoker()).invokeAsync(
            ArgumentMatchers.argThat(i -> i.equals(expected)),
                ArgumentMatchers.argThat(cl -> {
                    cl.forEach(c -> c.accept(expectedInvocationResult));
                    return true;
                }), ArgumentMatchers.argThat((InvocationErrorConsumer ec) -> {
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
        Mockito.when(objectFuture.get()).thenReturn(42);

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello World!");

        final int result = integerFuture.get();
        assertEquals(result, 42);
        Mockito.verify(objectFuture).get();

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));
        expected.setDispatchType(Dispatch.Type.FUTURE);

        Mockito.verify(getMockRemoteInvoker()).invokeFuture(
            ArgumentMatchers.eq(expected),
            ArgumentMatchers.eq(emptyList()), ArgumentMatchers.any(InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testAsyncReturnFutureWithConsumers() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        Mockito.when(objectFuture.get()).thenReturn(42);

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello World!", resultHandler, throwableConsumer);
        Mockito.verify(objectFuture, Mockito.never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        Mockito.verify(objectFuture, Mockito.times(1)).get();


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

        Mockito.verify(getMockRemoteInvoker()).invokeFuture(
            ArgumentMatchers.argThat(i -> i.equals(expected)),
            ArgumentMatchers.argThat(cl -> {
                cl.forEach(c -> c.accept(expectedInvocationResult));
                return true;
            }), ArgumentMatchers.any(InvocationErrorConsumer.class)
        );

    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumers() throws Exception {

        final Future<Object> objectFuture = setupMockToReturnFuture();
        Mockito.when(objectFuture.get()).thenReturn(42);

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final TestServiceInterface.MyStringHandler resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final TestServiceInterface.MyErrorHandler errorHandler = ex -> ex.equals(expectedRuntimeException);

        final Future<Integer> integerFuture = getTestServiceInterface().testAsyncReturnFuture("Hello World!", resultHandler, errorHandler);
        Mockito.verify(objectFuture, Mockito.never()).get();
        assertEquals(integerFuture.get(), Integer.valueOf(42));
        Mockito.verify(objectFuture, Mockito.times(1)).get();


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

        Mockito.verify(getMockRemoteInvoker()).invokeFuture(
            ArgumentMatchers.argThat(i -> i.equals(expected)),
            ArgumentMatchers.argThat(cl -> {
                cl.forEach(c -> c.accept(expectedInvocationResult));
                return true;
            }), ArgumentMatchers.argThat((InvocationErrorConsumer ec) -> {
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
        final Future<Object> future = Mockito.mock(Future.class);
        Mockito.when(getMockRemoteInvoker().invokeFuture(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(future);
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
            final RemoteInvoker remoteInvoker = Mockito.mock(RemoteInvoker.class);
            bind(RemoteInvoker.class).toInstance(remoteInvoker);
            bind(TestServiceInterface.class).toProvider(new RemoteProxyProvider<>(TestServiceInterface.class));
        }

    }

}
