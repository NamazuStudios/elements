package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Reflection;
import com.namazustudios.socialengine.rt.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.namazustudios.socialengine.rt.Reflection.*;
import static java.util.Arrays.fill;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * Builds an instance of {@link InvocationHandler} based on the underlying {@link Method} and {@link RemoteInvoker}.
 */
public class RemoteInvocationHandlerBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RemoteInvocationHandlerBuilder.class);

    private String name;

    private final Class<?> type;

    private final Method method;

    private final Routing routing;

    private final Dispatch.Type dispatchType;

    private final Supplier<ReturnValueTransformer> returnValueTransformerSupplier;

    private final Supplier<Function<Object[], List<Object>>> addressAssemblerSupplier;

    public RemoteInvocationHandlerBuilder(final RemoteInvoker remoteInvoker,
                                          final Class<?> type,
                                          final Method method) {

        final RemotelyInvokable remotelyInvokable = method.getAnnotation(RemotelyInvokable.class);

        if (remotelyInvokable == null) {
            throw new IllegalArgumentException(format(method) + " is not annotated with @RemotelyInvokable");
        }

        this.type = type;
        this.method = method;
        this.dispatchType = Dispatch.Type.determine(method);
        this.routing = remotelyInvokable.routing();

        this.addressAssemblerSupplier = () -> o -> emptyList();
        this.returnValueTransformerSupplier = () -> getReturnValueTransformer(remoteInvoker);

        switch (dispatchType) {
            case HYBRID:
            case CONSUMER:
                logger.warn("Using dispatch type {} for method {}.  Usage is discouraged.", dispatchType, format(method));
                break;
        }

    }

    public RemoteInvocationHandlerBuilder(final RemoteInvocationDispatcher remoteInvocationDispatcher,
                                          final Class<?> type,
                                          final Method method) {


        final RemotelyInvokable remotelyInvokable = method.getAnnotation(RemotelyInvokable.class);

        if (remotelyInvokable == null) {
            throw new IllegalArgumentException(format(method) + " is not annotated with @RemotelyInvokable");
        }

        this.type = type;
        this.method = method;
        this.dispatchType = Dispatch.Type.determine(method);
        this.routing = remotelyInvokable.routing();

        this.addressAssemblerSupplier = this::getAddressAssembler;
        this.returnValueTransformerSupplier = () -> getReturnValueTransformer(remoteInvocationDispatcher);

        switch (dispatchType) {
            case HYBRID:
            case CONSUMER:
                logger.warn("Using dispatch type {} for method {}.  Usage is discouraged.", dispatchType, format(method));
                break;
        }

    }

    /**
     * Gets the type of the remote object to invoke.  Specifies {@link Invocation#getType()}.
     *
     * @return the type
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Gets the name of the remote object to invoke.  Specifies {@link Invocation#getName()}.
     *
     * @return the type
     */
    public String getName() {
        return name;
    }

    /**
     * Gets {@link Method}.  Specifies {@link Invocation#getName()}.
     *
     * @return the type
     */
    public Method getMethod() {
        return method;
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
     * Sets the name of the remote object to invoke.  {@link Invocation#getName()}.
     *
     * @param name may be null if no name is used.
     * @return this instance
     */
    public RemoteInvocationHandlerBuilder withName(final String name) {
        this.name = name;
        return this;
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

        final ReturnValueTransformer returnValueTransformer;
        returnValueTransformer = returnValueTransformerSupplier.get();

        final Function<Object[], List<Object>> parameterAssembler;
        parameterAssembler = getParameterAssembler();

        final Function<Object[], List<Object>> addressAssembler = addressAssemblerSupplier.get();

        final Function<Object[], InvocationErrorConsumer> invocationErrorConsumerAssembler;
        invocationErrorConsumerAssembler = getInvocationErrorConsumerAssembler();

        final BiFunction<Object[], InvocationErrorConsumer, List<Consumer<InvocationResult>>> invocationResultConsumerAssembler;
        invocationResultConsumerAssembler = getInvocationResultConsumerListAssembler();

        final List<String> parameters;
        parameters = stream(method.getParameterTypes()).map(c -> c.getName()).collect(toList());

        final Class<? extends RoutingStrategy> routingStrategyType = routing.value();
        final String routingStrategyName = routing.name().isEmpty() ? null : routing.name();

        return (proxy, method1, args) -> {

            final Route route = new Route();
            route.setAddress(addressAssembler.apply(args));
            route.setRoutingStrategyType(routingStrategyType);
            route.setRoutingStrategyName(routingStrategyName);

            final Invocation invocation = new Invocation();

            invocation.setDispatchType(getDispatchType());
            invocation.setType(getType().getName());
            invocation.setName(getName());
            invocation.setMethod(getMethod().getName());
            invocation.setParameters(parameters);
            invocation.setArguments(parameterAssembler.apply(args));

            final InvocationErrorConsumer invocationErrorConsumer;
            invocationErrorConsumer = invocationErrorConsumerAssembler.apply(args);

            final List<Consumer<InvocationResult>> invocationResultConsumerList;
            invocationResultConsumerList = invocationResultConsumerAssembler.apply(args, invocationErrorConsumer);

            return returnValueTransformer.transform(route, invocation, invocationResultConsumerList, invocationErrorConsumer);

        };

    }
    private ReturnValueTransformer getReturnValueTransformer(final RemoteInvoker remoteInvoker) {

        final Dispatch.Type type = getDispatchType();

        switch (type) {
            case SYNCHRONOUS:
                return (r, i, ai, ae) -> remoteInvoker.invokeSync(i, ai, ae);
            case ASYNCHRONOUS:
                return (r, i, ai, ae) -> remoteInvoker.invokeAsync(i, ai, ae);
            case FUTURE:
                return (r, i, ai, ae) -> remoteInvoker.invokeFuture(i, ai, ae);
            case HYBRID:
            case CONSUMER:
                return isVoidMethod()   ? (r, i, ai, ae) -> remoteInvoker.invokeAsync(i, ai, ae) :
                       isFutureMethod() ? (r, i, ai, ae) -> remoteInvoker.invokeFuture(i, ai, ae) :
                                          (r, i, ai, ae) -> remoteInvoker.invokeSync(i, ai, ae);
            default:
                throw new IllegalArgumentException("Unknown dispatch type: " + type);
        }

    }

    private ReturnValueTransformer getReturnValueTransformer(final RemoteInvocationDispatcher remoteInvocationDispatcher) {

        final Dispatch.Type type = getDispatchType();

        switch (type) {
            case SYNCHRONOUS:
                return remoteInvocationDispatcher::invokeSync;
            case ASYNCHRONOUS:
                return remoteInvocationDispatcher::invokeAsync;
            case FUTURE:
                return remoteInvocationDispatcher::invokeFuture;
            case HYBRID:
            case CONSUMER:
                return isVoidMethod()   ? remoteInvocationDispatcher::invokeAsync :
                       isFutureMethod() ? remoteInvocationDispatcher::invokeFuture :
                                          remoteInvocationDispatcher::invokeSync;
            default:
                throw new IllegalArgumentException("Unknown dispatch type: " + type);
        }

    }

    private boolean isVoidMethod() {
        final Class<?> rType = getMethod().getReturnType();
        return void.class.equals(rType) || Void.class.equals(rType);
    }

    private boolean isFutureMethod() {
        return Future.class.isAssignableFrom(getMethod().getReturnType());
    }

    private Function<Object[], List<Object>> getParameterAssembler() {
        final Method method = getMethod();
        final int[] indices = indices(method, Serialize.class);
        return objects -> stream(indices).mapToObj(index -> objects[index]).collect(toList());
    }

    private Function<Object[], List<Object>> getAddressAssembler() {
        final Method method = getMethod();
        final int[] indices = indices(method, ProvidesAddress.class);
        return objects -> stream(indices).mapToObj(index -> objects[index]).collect(toList());
    }

    private Function<Object[], InvocationErrorConsumer> getInvocationErrorConsumerAssembler() {

        final Method method = getMethod();
        final int index = Reflection.errorHandlerIndex(method);

        if (index < 0) {
            return objects -> invocationError -> logger.error("Got invocation error.", invocationError.getThrowable());
        }

        final Parameter parameter = method.getParameters()[index];
        final Method errorHandlerMethod = getHandlerMethod(parameter);

        return objects -> {

            final AtomicBoolean called = new AtomicBoolean();

            return invocationError -> {
                try {

                    final Throwable throwable = invocationError.getThrowable();

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
                        handlerMethod.invoke(object, invocationResult.getResult());
                    } catch (IllegalAccessException e) {
                        logger.error("Caught exception executing handler.", e);
                        final InvocationError invocationError = new InvocationError();
                        invocationError.setThrowable(e);
                        errorConsumer.acceptAndLogError(logger, invocationError);
                    } catch (InvocationTargetException e) {
                        logger.info("Caught exception calling handler.", e.getTargetException());
                        final InvocationError invocationError = new InvocationError();
                        invocationError.setThrowable(e.getTargetException());
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
        Object transform(Route route,
                         Invocation invocation,
                         List<Consumer<InvocationResult>> asyncInvocationResultConsumerList,
                         InvocationErrorConsumer asyncInvocationErrorConsumer) throws Throwable;

    }

}
