package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.remote.TestServiceInterface;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.remote.*;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@Guice(modules = IoCInvocationDispatcherUnitTest.Module.class)
public class IoCInvocationDispatcherUnitTest {

    private InvocationDispatcher invocationDispatcher;

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

        final Consumer<InvocationError> invocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> returnInvocationResultConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        getInvocationDispatcher().dispatch(invocation,
                                           invocationErrorConsumer,
                                           returnInvocationResultConsumer,
                                           additionalInvocationResultConsumerList);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(null);

        verify(invocationErrorConsumer, never()).accept(any());
        verify(returnInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testSyncVoid(eq("Hello World!"));

    }

    @Test
    public void testDefaultMethod() throws Exception {

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testDefaultMethod");
        invocation.setParameters(emptyList());
        invocation.setArguments(emptyList());

        final Consumer<InvocationError> invocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> returnInvocationResultConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        getInvocationDispatcher().dispatch(invocation,
                invocationErrorConsumer,
                returnInvocationResultConsumer,
                additionalInvocationResultConsumerList);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(null);

        verify(invocationErrorConsumer, never()).accept(any());
        verify(returnInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testDefaultMethod();

    }


    @Test
    public void testSyncReturn() throws Exception {

        final Invocation invocation = new Invocation();

        invocation.setType(TestServiceInterface.class.getName());
        invocation.setMethod("testSyncReturn");
        invocation.setParameters(asList(String.class.getName()));
        invocation.setArguments(asList("Hello World!"));

        final Consumer<InvocationError> invocationErrorConsumer = mock(Consumer.class);
        final Consumer<InvocationResult> returnInvocationResultConsumer = mock(Consumer.class);
        final List<Consumer<InvocationResult>> additionalInvocationResultConsumerList = emptyList();

        when(getMockTestServiceInterface().testSyncReturn(any())).thenReturn(4.2);

        getInvocationDispatcher().dispatch(invocation,
                invocationErrorConsumer,
                returnInvocationResultConsumer,
                additionalInvocationResultConsumerList);

        final InvocationResult expected = new InvocationResult();
        expected.setResult(4.2);

        verify(invocationErrorConsumer, never()).accept(any());
        verify(returnInvocationResultConsumer, times(1)).accept(eq(expected));
        verify(getMockTestServiceInterface(), times(1)).testSyncReturn(eq("Hello World!"));

    }

    @Test
    public void testAsyncVoid() throws Exception {


        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnVoid");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        final RuntimeException expectedRuntimeException = new RuntimeException();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

    }

    @Test
    public void testAsyncReturnFuture() throws Exception {

        final Invocation expected = new Invocation();

        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName()));
        expected.setArguments(asList("Hello World!"));

    }

    @Test
    public void testAsyncReturnFutureWithConsumers() throws Exception {

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final Consumer<String> resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final Consumer<Throwable> throwableConsumer = ex -> ex.equals(expectedRuntimeException);


        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName(), Consumer.class.getName(), Consumer.class.getName()));
        expected.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

    }

    @Test
    public void testAsyncReturnFutureWithCustomConsumers() throws Exception {

        final RuntimeException expectedRuntimeException = new RuntimeException();
        final TestServiceInterface.MyStringHandler resultHandler = r -> assertEquals("Why, hello to you as well!", r);
        final TestServiceInterface.MyErrorHandler errorHandler = ex -> ex.equals(expectedRuntimeException);

        final Invocation expected = new Invocation();
        expected.setType(TestServiceInterface.class.getName());
        expected.setMethod("testAsyncReturnFuture");
        expected.setParameters(asList(String.class.getName(), TestServiceInterface.MyStringHandler.class.getName(), TestServiceInterface.MyErrorHandler.class.getName()));
        expected.setArguments(asList("Hello World!"));

        final InvocationError expectedInvocationError = new InvocationError();
        expectedInvocationError.setThrowable(expectedRuntimeException);

        final InvocationResult expectedInvocationResult = new InvocationResult();
        expectedInvocationResult.setResult("Why, hello to you as well!");

    }

    public InvocationDispatcher getInvocationDispatcher() {
        return invocationDispatcher;
    }

    @Inject
    public void setInvocationDispatcher(InvocationDispatcher invocationDispatcher) {
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
            bind(InvocationDispatcher.class).to(IoCInvocationDispatcher.class);

        }

    }

}
