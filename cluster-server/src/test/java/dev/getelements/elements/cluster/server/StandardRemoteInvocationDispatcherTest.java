package dev.getelements.elements.cluster.server;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ElementScope;
import dev.getelements.elements.sdk.ElementType;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.address.ElementAddress;
import dev.getelements.elements.sdk.cluster.address.RemoteInstanceSelector;
import dev.getelements.elements.sdk.cluster.id.DeploymentId;
import dev.getelements.elements.sdk.cluster.remote.RemoteInvocationDispatcher.ResultAsyncHandler;
import dev.getelements.elements.sdk.cluster.remote.annotation.Dispatch;
import dev.getelements.elements.sdk.cluster.remote.annotation.ErrorHandler;
import dev.getelements.elements.sdk.cluster.remote.annotation.ResultHandler;
import dev.getelements.elements.sdk.cluster.remote.annotation.Serialize;
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;
import dev.getelements.elements.sdk.cluster.remote.exception.MethodNotFoundException;
import dev.getelements.elements.sdk.cluster.remote.exception.ServiceNotFoundException;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementServiceKey;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class StandardRemoteInvocationDispatcherTest {

    private static final String ELEMENT_NAME = "test-element";

    /**
     * dispatch() now offloads to an executor and returns immediately, so tests capture whichever handler
     * method fires (exactly one, per scenario) via this future and block on it instead of asserting synchronously.
     */
    private CompletableFuture<Object> pushed;

    private ResultAsyncHandler handler;

    private StandardRemoteInvocationDispatcher dispatcher;

    @BeforeMethod
    public void setUp() {
        dispatcher = new StandardRemoteInvocationDispatcher();
        pushed = new CompletableFuture<>();
        handler = new ResultAsyncHandler() {
            @Override
            public void onInvocationResult(final InvocationResult result) {
                pushed.complete(result);
            }

            @Override
            public void onInvocationError(final InvocationError error) {
                pushed.complete(error);
            }
        };
    }

    private Object await() throws Exception {
        return pushed.get(5, TimeUnit.SECONDS);
    }

    @Test
    public void dispatchesSynchronousInvocation() throws Exception {

        final var deploymentId = registerRuntime("deployment-sync", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId, "echo", List.of("java.lang.String"), List.of("hello"), Dispatch.Type.SYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        final var result = (InvocationResult) await();
        assertEquals(result.param(), InvocationResult.RETURN_VALUE);
        assertEquals(result.result(), "hello");

    }

    @Test
    public void methodExceptionIsUnwrappedFromInvocationTargetException() throws Exception {

        final var deploymentId = registerRuntime("deployment-explode", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId, "explode", List.of("java.lang.String"), List.of("x"), Dispatch.Type.SYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        final var error = (InvocationError) await();
        assertTrue(error.throwable() instanceof IllegalStateException);
        assertEquals(error.throwable().getMessage(), "boom: x");

    }

    @Test
    public void missingDeploymentProducesServiceNotFoundError() throws Exception {

        final var deploymentId = DeploymentId.forUniqueName("never-registered");

        final var invocation = newInvocation(
                deploymentId, "echo", List.of("java.lang.String"), List.of("hi"), Dispatch.Type.SYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        assertServiceNotFoundError();

    }

    @Test
    public void missingElementProducesServiceNotFoundError() throws Exception {

        final var deploymentId = registerRuntime("deployment-missing-element", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId, "nonexistent-element", "echo",
                List.of("java.lang.String"), List.of("hi"), Dispatch.Type.SYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        assertServiceNotFoundError();

    }

    @Test
    public void missingMethodProducesMethodNotFoundError() throws Exception {

        final var deploymentId = registerRuntime("deployment-missing-method", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId, "doesNotExist", List.of(), List.of(), Dispatch.Type.SYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        final var error = (InvocationError) await();
        assertTrue(error.throwable() instanceof MethodNotFoundException);

    }

    @Test
    public void asynchronousDispatchDeliversResultViaResultHandlerProxy() throws Exception {

        final var deploymentId = registerRuntime("deployment-async-result", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId,
                "echoAsync",
                List.of("java.lang.String", "java.util.function.Consumer", "java.util.function.Consumer"),
                List.of("hello"),
                Dispatch.Type.ASYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        final var result = (InvocationResult) await();
        assertEquals(result.param(), 1);
        assertEquals(result.result(), "hello!");

    }

    @Test
    public void asynchronousDispatchDeliversErrorViaErrorHandlerProxy() throws Exception {

        final var deploymentId = registerRuntime("deployment-async-error", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId,
                "echoAsync",
                List.of("java.lang.String", "java.util.function.Consumer", "java.util.function.Consumer"),
                List.of("fail"),
                Dispatch.Type.ASYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        final var error = (InvocationError) await();
        assertTrue(error.throwable() instanceof IllegalArgumentException);
        assertEquals(error.throwable().getMessage(), "nope");

    }

    @Test
    public void asynchronousDispatchWithMultipleResultHandlersUsesRawParameterIndices() throws Exception {

        final var deploymentId = registerRuntime("deployment-async-multi", new TestServiceImpl());

        final List<InvocationResult> results = new CopyOnWriteArrayList<>();
        final CountDownLatch latch = new CountDownLatch(2);

        final ResultAsyncHandler multiHandler = new ResultAsyncHandler() {
            @Override
            public void onInvocationResult(final InvocationResult result) {
                results.add(result);
                latch.countDown();
            }

            @Override
            public void onInvocationError(final InvocationError error) {
                latch.countDown();
            }
        };

        final var invocation = newInvocation(
                deploymentId,
                "echoAsyncMulti",
                List.of(
                        "java.lang.String",
                        "java.util.function.Consumer",
                        "java.util.function.Consumer",
                        "java.util.function.Consumer"),
                List.of("hello"),
                Dispatch.Type.ASYNCHRONOUS);

        dispatcher.dispatch(invocation, multiHandler);

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(results.size(), 2);

        // Raw/actual parameter indices (value=0, onFirst=1, onSecond=2, onError=3) - NOT ordinals among
        // only the @ResultHandler params (which would incorrectly be 0 and 1).
        final var first = results.stream().filter(r -> r.param() == 1).findFirst().orElseThrow();
        final var second = results.stream().filter(r -> r.param() == 2).findFirst().orElseThrow();

        assertEquals(first.result(), "hello-first");
        assertEquals(second.result(), "hello-second");

    }

    @Test
    public void futureDispatchResolvesAndPushesSyncResult() throws Exception {

        final var deploymentId = registerRuntime("deployment-future", new TestServiceImpl());

        final var invocation = newInvocation(
                deploymentId, "echoFuture", List.of("java.lang.String"), List.of("hello"), Dispatch.Type.FUTURE);

        dispatcher.dispatch(invocation, handler);

        final var result = (InvocationResult) await();
        assertEquals(result.param(), InvocationResult.RETURN_VALUE);
        assertEquals(result.result(), "HELLO");

    }

    @Test
    public void runtimeUnloadedRemovesRuntimeFromMap() throws Exception {

        final var uniqueDeploymentName = "deployment-unload";
        final var deploymentId = registerRuntime(uniqueDeploymentName, new TestServiceImpl());

        dispatcher.onRuntimeUnloaded(uniqueDeploymentName);

        final var invocation = newInvocation(
                deploymentId, "echo", List.of("java.lang.String"), List.of("hi"), Dispatch.Type.SYNCHRONOUS);

        dispatcher.dispatch(invocation, handler);

        assertServiceNotFoundError();

    }

    private void assertServiceNotFoundError() throws Exception {
        final var error = (InvocationError) await();
        assertTrue(error.throwable() instanceof ServiceNotFoundException);
    }

    private DeploymentId registerRuntime(final String uniqueDeploymentName, final Object serviceInstance) {

        final var element = newElement(ELEMENT_NAME, TestService.class, serviceInstance);

        final var deployment = new ElementDeployment(
                uniqueDeploymentName, null, null, null, null, null,
                null, null, false, null, null, 0L);

        final var runtimeRecord = new ElementRuntimeService.RuntimeRecord(
                deployment,
                ElementRuntimeService.RuntimeStatus.CLEAN,
                false,
                null,
                List.of(element),
                null, null, null, null, null, null, null, null, null);

        dispatcher.onRuntimeLoaded(runtimeRecord);

        return DeploymentId.forUniqueName(uniqueDeploymentName);

    }

    private Invocation newInvocation(
            final DeploymentId deploymentId,
            final String methodName,
            final List<String> parameterTypeNames,
            final List<Object> arguments,
            final Dispatch.Type dispatchType) {
        return newInvocation(deploymentId, ELEMENT_NAME, methodName, parameterTypeNames, arguments, dispatchType);
    }

    private Invocation newInvocation(
            final DeploymentId deploymentId,
            final String elementName,
            final String methodName,
            final List<String> parameterTypeNames,
            final List<Object> arguments,
            final Dispatch.Type dispatchType) {

        final var instanceSelector = new RemoteInstanceSelector(null, deploymentId);
        final var elementAddress = instanceSelector.withElement(elementName, ElementAddress.DEFAULT_INDEX);
        final var serviceAddress = elementAddress.withService(TestService.class.getName());
        final var methodAddress = serviceAddress.withMethod(methodName, parameterTypeNames);

        return new Invocation(
                methodAddress,
                parameterTypeNames,
                arguments,
                dispatchType);

    }

    private static Element newElement(
            final String elementName,
            final Class<?> serviceInterface,
            final Object serviceInstance) {

        final var definition = new ElementDefinitionRecord(
                StandardRemoteInvocationDispatcherTest.class.getPackage(),
                elementName,
                false,
                List.of(),
                null);

        final var elementRecord = new ElementRecord(
                ElementType.SHARED_CLASSPATH,
                definition,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                StandardRemoteInvocationDispatcherTest.class.getClassLoader());

        final ServiceLocator serviceLocator = new ServiceLocator() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> Optional<Supplier<T>> findInstance(final ElementServiceKey<T> key) {
                if (serviceInterface.equals(key.type())) {
                    final Supplier<T> supplier = () -> (T) serviceInstance;
                    return Optional.of(supplier);
                }
                return Optional.empty();
            }
        };

        return new Element() {

            @Override
            public ElementRecord getElementRecord() {
                return elementRecord;
            }

            @Override
            public ServiceLocator getServiceLocator() {
                return serviceLocator;
            }

            @Override
            public ElementRegistry getElementRegistry() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ElementScope.Builder withScope() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Optional<ElementScope> findCurrentScope() {
                return Optional.empty();
            }

            @Override
            public void publish(final Event event) {
            }

            @Override
            public Subscription onClose(final Consumer<Element> onClose) {
                return () -> {};
            }

            @Override
            public void close() {
            }

        };

    }

    private interface TestService {

        String echo(@Serialize String value);

        String explode(@Serialize String value);

        void echoAsync(
                @Serialize String value,
                @ResultHandler Consumer<String> onResult,
                @ErrorHandler Consumer<Throwable> onError);

        void echoAsyncMulti(
                @Serialize String value,
                @ResultHandler Consumer<String> onFirst,
                @ResultHandler Consumer<String> onSecond,
                @ErrorHandler Consumer<Throwable> onError);

        Future<String> echoFuture(@Serialize String value);

    }

    private static class TestServiceImpl implements TestService {

        @Override
        public String echo(final String value) {
            return value;
        }

        @Override
        public String explode(final String value) {
            throw new IllegalStateException("boom: " + value);
        }

        @Override
        public void echoAsync(final String value, final Consumer<String> onResult, final Consumer<Throwable> onError) {
            if ("fail".equals(value)) {
                onError.accept(new IllegalArgumentException("nope"));
            } else {
                onResult.accept(value + "!");
            }
        }

        @Override
        public void echoAsyncMulti(
                final String value,
                final Consumer<String> onFirst,
                final Consumer<String> onSecond,
                final Consumer<Throwable> onError) {
            onFirst.accept(value + "-first");
            onSecond.accept(value + "-second");
        }

        @Override
        public Future<String> echoFuture(final String value) {
            return CompletableFuture.completedFuture(value.toUpperCase());
        }

    }

}
