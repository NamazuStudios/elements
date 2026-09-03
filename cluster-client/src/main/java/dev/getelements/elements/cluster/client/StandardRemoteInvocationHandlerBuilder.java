package dev.getelements.elements.cluster.client;

import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.annotation.Disabled;
import dev.getelements.elements.sdk.cluster.address.RemoteElementAddress;
import dev.getelements.elements.sdk.cluster.address.RemoteElementMethodAddress;
import dev.getelements.elements.sdk.cluster.remote.AsyncOperation;
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer;
import dev.getelements.elements.sdk.cluster.remote.RemoteInvoker;
import dev.getelements.elements.sdk.cluster.remote.annotation.*;
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError;
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;
import dev.getelements.elements.sdk.cluster.remote.routing.AddressingStrategy;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.record.ElementServiceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.getelements.elements.sdk.cluster.util.Reflection.*;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Builds an instance of {@link InvocationHandler} based on the underlying {@link Method} and {@link RemoteInvoker}.
 */
public class StandardRemoteInvocationHandlerBuilder<ProxyInterfaceT> {

    private static final Logger logger = LoggerFactory.getLogger(StandardRemoteInvocationHandlerBuilder.class);

    private final Method method;

    private final Routing routing;

    private final Addressing addressing;

    private final Dispatch.Type dispatchType;

    private final ServiceLocator serviceLocator;

    private final RemoteInvoker remoteInvoker;

    private final ElementServiceKey<ProxyInterfaceT> serviceKey;

    private final RemoteElementAddress remoteElementAddress;

    public StandardRemoteInvocationHandlerBuilder(
            final ServiceLocator serviceLocator,
            final RemoteInvoker remoteInvoker,
            final RemoteElementAddress remoteElementAddress,
            final ElementServiceKey<ProxyInterfaceT> serviceKey,
            final Method method) {

        final RemotelyInvokable remotelyInvokable = method.getAnnotation(RemotelyInvokable.class);

        if (remotelyInvokable == null) {
            throw new IllegalArgumentException(format(method) + " is not annotated with @RemotelyInvokable");
        }

        this.serviceLocator = serviceLocator;
        this.serviceKey = serviceKey;
        this.method = method;
        this.dispatchType = Dispatch.Type.determine(method);
        this.routing = remotelyInvokable.routing();
        this.addressing = remotelyInvokable.addressing();
        this.remoteInvoker = remoteInvoker;
        this.remoteElementAddress = remoteElementAddress;

    }

    public Method getMethod() {
        return method;
    }

    public ElementServiceKey<ProxyInterfaceT> getServiceKey() {
        return serviceKey;
    }

    /**
     * Returns the {@link Dispatch.Type} of this method.
     *
     * @return the type of dispatch
     */
    public Dispatch.Type getDispatchType() {
        return dispatchType;
    }

    /**
     * Builds the {@link InvocationHandler} which will dispach calls to hte remote method.  This does so by building
     * as much as possible ahead of time in order to provide type checking before methods are ever called.  Secondly,
     * the returned {@link InvocationHandler} performs minimal work to actually shuffle the arguments around when
     * implementing the request/response schema.
     *
     * @return the {@link InvocationHandler}
     */
    public InvocationHandler build() {

        logger.info("Building invocation handler for {} with dispatch type {}", format(method), getDispatchType());

        final var parameterAssembler = getParameterAssembler();
        final var returnValueTransformer = getReturnValueTransformer();
        final var invocationErrorConsumerAssembler = getInvocationErrorConsumerAssembler();
        final var invocationResultConsumerAssembler = getInvocationResultConsumerListAssembler();

        final var parameters = stream(method.getParameterTypes())
                .map(Class::getName)
                .collect(toList());

        final var remoteElementMethodAddress = remoteElementAddress
                .withService(serviceKey.type().getName(), serviceKey.name())
                .withMethod(method.getName(), parameters);

        final var addressAssembler = getAddressAssembler(remoteElementMethodAddress);

        return (proxy, _m, args) -> {

            final var resolved = addressAssembler.apply(args);

            final Invocation invocation = new Invocation(
                resolved,
                parameters,
                parameterAssembler.apply(args),
                getDispatchType());

            final InvocationErrorConsumer invocationErrorConsumer;
            invocationErrorConsumer = invocationErrorConsumerAssembler.apply(args);

            final List<Consumer<InvocationResult>> invocationResultConsumerList;
            invocationResultConsumerList = invocationResultConsumerAssembler.apply(args, invocationErrorConsumer);

            return returnValueTransformer.transform(invocation, invocationResultConsumerList, invocationErrorConsumer);

        };

    }
    private ReturnValueTransformer getReturnValueTransformer() {

        final Dispatch.Type type = getDispatchType();

        return switch (type) {
            case SYNCHRONOUS -> (i, ai, ae) -> remoteInvoker.invokeSync(i, ai, ae);
            case ASYNCHRONOUS -> isAsyncMethod() ? (i, ai, ae) -> remoteInvoker.invokeAsync(i, ai, ae) :
                    (i, ai, ae) -> remoteInvoker.invokeAsyncV(i, ai, ae);
            case FUTURE -> (i, ai, ae) -> remoteInvoker.invokeFuture(i, ai, ae);
            default -> throw new IllegalArgumentException("Unknown dispatch type: " + type);
        };

    }

    private boolean isVoidMethod() {
        final Class<?> rType = getMethod().getReturnType();
        return void.class.equals(rType) || Void.class.equals(rType);
    }

    private boolean isAsyncMethod() {
        final Class<?> rType = getMethod().getReturnType();
        return AsyncOperation.class.isAssignableFrom(rType);
    }

    private boolean isFutureMethod() {
        return Future.class.isAssignableFrom(getMethod().getReturnType());
    }

    private Function<Object[], List<Object>> getParameterAssembler() {
        final Method method = getMethod();
        final int[] indices = indices(method, Serialize.class);
        return objects -> stream(indices)
                .mapToObj(index -> objects[index])
                .collect(toList());
    }

    private Function<Object[], RemoteElementMethodAddress>  getAddressAssembler(final RemoteElementMethodAddress remoteElementMethodAddress) {
        final var method = getMethod();
        final var indices = indices(method, RoutingAddressSource.class);
        final var addressingStrategy = getAddressingStrategy();
        return objects -> addressingStrategy.resolve(remoteElementMethodAddress, stream(indices)
                .mapToObj(index -> objects[index])
                .toArray());
    }

    private AddressingStrategy getAddressingStrategy() {

        final var reference = addressing.service();

        if (Disabled.class.equals(reference.value())) {
            try {
                return addressing.value().getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException ex) {
                throw new InternalException(ex);
            }
        }

        final var addressingStrategyServiceKey = ElementServiceKey.from(reference);
        return (AddressingStrategy) serviceLocator.getInstance(addressingStrategyServiceKey);

    }

    private Function<Object[], InvocationErrorConsumer> getInvocationErrorConsumerAssembler() {

        final Method method = getMethod();
        final int index = errorHandlerIndex(method);

        if (index < 0) {
            return objects -> invocationError -> logger.error("Got invocation error.", invocationError.throwable());
        }

        final Parameter parameter = method.getParameters()[index];
        final Method errorHandlerMethod = getHandlerMethod(parameter);

        return objects -> {

            final AtomicBoolean called = new AtomicBoolean();

            return invocationError -> {
                try {

                    final Throwable throwable = invocationError.throwable();

                    if (called.getAndSet(true)) {
                        // Remote calls may end up sending multiple errors for any number of reasons, so we ensure
                        // that the error handler we do use actually only gets called just once.
                        logger.info("Additional errors invoking method {}", format(method));
                    } else {
                        errorHandlerMethod.invoke(objects[index], throwable);
                    }

                } catch (IllegalAccessException | InvocationTargetException ex) {
                    logger.error("Caught Exception passing Exception to Exception handler (It's confusing, I know, but trust me.)", ex);
                }
            };

        };

    }

    private BiFunction<Object[], InvocationErrorConsumer, List<Consumer<InvocationResult>>> getInvocationResultConsumerListAssembler() {

        final Method method = getMethod();
        final int[] resultHandlerIndices = indices(method, ResultHandler.class);
        final Parameter[] parameters = method.getParameters();

        final Method[] resultHandlerMethods = stream(resultHandlerIndices)
            .mapToObj(index -> {
                try {
                    return getHandlerMethod(parameters[index]);
                } catch (IllegalArgumentException ex) {
                    return null;
                }
            }).toArray(Method[]::new);

        return (objects, errorConsumer) -> {

            final Iterator<Method> resultHandlerMethodIterator = stream(resultHandlerMethods).iterator();

            return stream(resultHandlerIndices).mapToObj(index -> {

                final Object object = objects[index];
                final Method handlerMethod = resultHandlerMethodIterator.next();

                return (Consumer<InvocationResult>) invocationResult -> {
                    try {
                        handlerMethod.invoke(object, invocationResult.result());
                    } catch (IllegalAccessException e) {
                        logger.error("Caught exception executing handler.", e);
                        final InvocationError invocationError = new InvocationError(e);
                        errorConsumer.acceptAndLogError(logger, invocationError);
                    } catch (InvocationTargetException e) {
                        logger.info("Caught exception calling handler.", e.getTargetException());
                        final InvocationError invocationError = new InvocationError(e.getTargetException());
                        errorConsumer.acceptAndLogError(logger, invocationError);
                    }
                };

            }).collect(toList());

        };

    }

    /**
     * Transforms the an Invocation and supporting arguments to a {@link Object} as the return value of a proxied
     * method call.  This is responsible for selecting the proper dispatch method of of the {@link RemoteInvoker} and
     * returning the result as an {@link Object}.
     */
    @FunctionalInterface
    private interface ReturnValueTransformer {

        /**
         * Performs the translation.  This will translate the return value and, if necessary, throw an instance of
         * {@link Throwable} if the remote {@link Method} failed.
         *
         * @return an {@link Object} to return from the {@link InvocationHandler}
         * @throws Throwable if an exception occurs, can also be re-throwing the remiote invocation error
         */
        Object transform(Invocation invocation,
                         List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                         InvocationErrorConsumer asyncInvocationErrorConsumer) throws Throwable;

    }

}
