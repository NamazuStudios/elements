package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.annotation.RemoteScope;
import com.namazustudios.socialengine.rt.remote.TestServiceInterface;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.remote.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.annotation.RemoteScope.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

@Guice(modules = IoCInvocationDispatcherUnitTest.Module.class)
public class IoCInvocationDispatcherUnitTest {

    private LocalInvocationDispatcher invocationDispatcher;

    private TestServiceInterface mockTestServiceInterface;

    @AfterMethod
    public void resetMocks() {
        reset(getMockTestServiceInterface());
    }

    @Test
    public void testSync() throws Exception {

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testSyncVoid");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> asyncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncasyncInvocationErrorConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        getInvocationDispatcher().dispatch(invocation,
                asyncInvocationResultConsumer, syncasyncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(null);

        verify(asyncInvocationErrorConsumer, never()).accept(any());
        verify(asyncInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testSyncVoid(eq("Hello World!"));

    }

    @Test
    public void testDefaultMethod() throws Exception {

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testDefaultMethod");
        invocation.setParameters(emptyList());
        invocation.setArguments(emptyList());

        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> asyncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncasyncInvocationErrorConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        getInvocationDispatcher().dispatch(invocation,
                asyncInvocationResultConsumer, syncasyncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(null);

        verify(asyncInvocationErrorConsumer, never()).accept(any());
        verify(asyncInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testDefaultMethod();

    }


    @Test
    public void testSyncReturn() throws Exception {

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testSyncReturn");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> asyncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncasyncInvocationErrorConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        when(getMockTestServiceInterface().testSyncReturn(any())).thenReturn(4.2);

        getInvocationDispatcher().dispatch(invocation,
                asyncInvocationResultConsumer, syncasyncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(4.2);

        verify(asyncInvocationErrorConsumer, never()).accept(any());
        verify(asyncInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testSyncReturn(eq("Hello World!"));

    }

    @Test
    public void testAsyncVoid() throws Exception {

        final Invocation invocation = new Invocation();
        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnVoid");
        invocation.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        final RuntimeException expectedRuntimeException = new RuntimeException();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> asyncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncasyncInvocationErrorConsumer = mock(Consumer.class);

        final Consumer<InvocationResult> argInvocationResultConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = asList(argInvocationResultConsumer);

        doAnswer(i -> {

            final Consumer<String> stringConsumer = i.getArgument(1);
            final Consumer<Throwable> throwableConsumer = i.getArgument(2);

            stringConsumer.accept("Why, hello to you as well!");
            throwableConsumer.accept(expectedRuntimeException);

            return null;

        }).when(getMockTestServiceInterface()).testAsyncReturnVoid(any(), any(), any());

        getInvocationDispatcher().dispatch(invocation,
                asyncInvocationResultConsumer, syncasyncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(null);

        verify(asyncInvocationErrorConsumer, times(1)).accept(expectedInvocationError);
        verify(syncasyncInvocationErrorConsumer, never()).accept(any());
        verify(getMockTestServiceInterface(), times(1))
            .testAsyncReturnVoid(eq("Hello World!"), any(Consumer.class), any(Consumer.class));

    }

    @Test
    public void testAsyncReturnFuture() throws Exception {

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnFuture");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final Consumer<InvocationResult> syncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncInvocationErrorConsumer = mock(Consumer.class);

        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        final Future<Integer> integerFuture = mock(Future.class);
        when(integerFuture.get()).thenReturn(42);
        when(getMockTestServiceInterface().testAsyncReturnFuture(any())).thenReturn(integerFuture);

        getInvocationDispatcher().dispatch(invocation,
                syncInvocationResultConsumer, syncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(42);

        verify(asyncInvocationErrorConsumer, never()).accept(any());
        verify(syncInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testAsyncReturnFuture(eq("Hello World!"));

    }

    @Test
    public void testAsyncReturnFutureWithConsumers() throws Exception {

        final Invocation invocation = new Invocation();
        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnFuture");
        invocation.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        final RuntimeException expectedRuntimeException = new RuntimeException();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        final Consumer<InvocationResult> syncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncInvocationErrorConsumer = mock(Consumer.class);

        final Consumer<InvocationResult> argInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = asList(argInvocationResultConsumer);

        final Future<Integer> integerFuture = mock(Future.class);
        when(integerFuture.get()).thenReturn(42);

        when(getMockTestServiceInterface().testAsyncReturnFuture(any(), any(Consumer.class), any(Consumer.class))).thenAnswer(i -> {

            final Consumer<String> stringConsumer = i.getArgument(1);
            final Consumer<Throwable> throwableConsumer = i.getArgument(2);

            stringConsumer.accept("Why, hello to you as well!");
            throwableConsumer.accept(expectedRuntimeException);

            return integerFuture;

        });

        getInvocationDispatcher().dispatch(invocation,
                syncInvocationResultConsumer, syncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(42);

        verify(asyncInvocationErrorConsumer, times(1)).accept(eq(expectedInvocationError));
        verify(syncInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1))
            .testAsyncReturnFuture(eq("Hello World!"), any(Consumer.class), any(Consumer.class));

    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumers() throws Exception {

        final Invocation invocation = new Invocation();
        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testAsyncReturnFuture");
        invocation.setParameters(asList(String.class.getName(),
                                        TestServiceInterface.MyStringHandler.class.getName(),
                                        TestServiceInterface.MyErrorHandler.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        final RuntimeException expectedRuntimeException = new RuntimeException();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

        final Consumer<InvocationResult> syncInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> syncInvocationErrorConsumer = mock(Consumer.class);

        final Consumer<InvocationResult> argInvocationResultConsumer = mock(Consumer.class);
        final Consumer<InvocationError> asyncInvocationErrorConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = asList(argInvocationResultConsumer);

        final Future<Integer> integerFuture = mock(Future.class);
        when(integerFuture.get()).thenReturn(42);

        when(getMockTestServiceInterface().testAsyncReturnFuture(any(), isA(TestServiceInterface.MyStringHandler.class), isA(TestServiceInterface.MyErrorHandler.class))).thenAnswer(i -> {

            final TestServiceInterface.MyStringHandler stringHandler = i.getArgument(1);
            final TestServiceInterface.MyErrorHandler throwableConsumer = i.getArgument(2);

            stringHandler.handle("Why, hello to you as well!");
            throwableConsumer.handle(expectedRuntimeException);

            return integerFuture;

        });

        getInvocationDispatcher().dispatch(invocation,
                syncInvocationResultConsumer, syncInvocationErrorConsumer,
                additionalInvocationResultConsumerList, asyncInvocationErrorConsumer);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(42);

        verify(asyncInvocationErrorConsumer, times(1)).accept(eq(expectedInvocationError));
        verify(syncInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1))
            .testAsyncReturnFuture(eq("Hello World!"),
                                   any(TestServiceInterface.MyStringHandler.class),
                                   any(TestServiceInterface.MyErrorHandler.class));

    }

    public LocalInvocationDispatcher getInvocationDispatcher() {
        return invocationDispatcher;
    }

    @Inject
    public void setInvocationDispatcher(LocalInvocationDispatcher invocationDispatcher) {
        this.invocationDispatcher = invocationDispatcher;
    }

    public TestServiceInterface getMockTestServiceInterface() {
        return mockTestServiceInterface;
    }

    @Inject
    public void setMockTestServiceInterface(TestServiceInterface mockTestServiceInterface) {
        this.mockTestServiceInterface = mockTestServiceInterface;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            final TestServiceInterface testServiceInterface = mock(TestServiceInterface.class);
            bind(TestServiceInterface.class).toInstance(testServiceInterface);

            bind(IocResolver.class).toInstance(new GuiceIoCResolver());
            bind(LocalInvocationDispatcher.class).to(IoCLocalInvocationDispatcher.class);

            bind(String.class)
                    .annotatedWith(named(REMOTE_SCOPE))
                    .toInstance(WORKER_SCOPE);

            bind(String.class)
                    .annotatedWith(named(REMOTE_PROTOCOL))
                    .toInstance(ELEMENTS_RT_PROTOCOL);

        }

    }

}
