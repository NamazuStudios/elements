package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.annotation.ErrorHandler;
import com.namazustudios.socialengine.rt.annotation.RemotelyInvokable;
import com.namazustudios.socialengine.rt.annotation.ResultHandler;
import com.namazustudios.socialengine.rt.annotation.Serialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.namazustudios.socialengine.rt.Reflection.format;
import static com.namazustudios.socialengine.rt.Reflection.methods;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * Builds an instance of {@link InvocationHandler} based on the underlying {@link Method} and {@link RemoteInvoker}.
 */
public class RemoteInvocationHandlerBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RemoteInvocationHandlerBuilder.class);

    private String name;

    private final Class<?> type;

    private final Method method;

    private final RemoteInvoker remoteInvoker;

    public RemoteInvocationHandlerBuilder(final RemoteInvoker remoteInvoker, final Class<?> type, final Method method) {

        if (method.getAnnotation(RemotelyInvokable.class) == null) {
            throw new IllegalArgumentException(format(method) + " is not annotated with @RemotelyInvokable");
        }

        this.type = type;
        this.method = method;
        this.remoteInvoker = remoteInvoker;

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

        final ReturnValueTransformer returnValueTransformer;
        returnValueTransformer = getReturnValueTransformer(getMethod());

        final Function<Object[], List<Object>> parameterAssembler;
        parameterAssembler = getParameterAssembler(getMethod());

        final Function<Object[], Consumer<InvocationResult>> invocationResultConsumerAssembler;
        invocationResultConsumerAssembler = getInvocationResultConsumerAssembler(getMethod());

        return (proxy, method1, args) -> {
            final Invocation invocation = new Invocation();

            invocation.setType(getType().getName());
            invocation.setName(getName());
            invocation.setMethod(getMethod().getName());
            invocation.setArguments(parameterAssembler.apply(args));

            final Consumer<InvocationResult> invocationResultConsumer;
            invocationResultConsumer = invocationResultConsumerAssembler.apply(args);

            final Future<Object> objectFuture = remoteInvoker.invoke(invocation, invocationResultConsumer);
            return returnValueTransformer.transform(objectFuture);

        };

    }

    private ReturnValueTransformer getReturnValueTransformer(final Method method) {
        return Future.class.isAssignableFrom(method.getReturnType()) ? future -> future : future -> {
            try {
                return future.get();
            } catch (ExecutionException ex) {
                throw ex.getCause();
            }
        };
    }

    private Function<Object[], List<Object>> getParameterAssembler(final Method method) {

        final Parameter[] parameters = method.getParameters();

        final int[] indices = IntStream
            .range(0, parameters.length)
            .filter(index -> parameters[index].getAnnotation(Serialize.class) != null)
            .toArray();

        return objects -> stream(indices).mapToObj(index -> objects[index]).collect(toList());

    }

    private Function<Object[], Consumer<InvocationResult>> getInvocationResultConsumerAssembler(final Method method) {

        final Parameter[] parameters = method.getParameters();

        final int[] resultHandlerIndices = IntStream
            .range(0, parameters.length)
            .filter(index -> parameters[index].getAnnotation(ResultHandler.class) != null)
            .toArray();

        final int[] errorHandlerIndices = IntStream
            .range(0, parameters.length)
            .filter(index -> parameters[index].getAnnotation(ErrorHandler.class) != null)
            .toArray();

        final Method[] resultHandlerMethods = stream(resultHandlerIndices)
            .mapToObj(index -> getHandlerMethod(index, method, parameters[index], Object.class))
            .toArray(Method[]::new);

        final Method[] errorHandlerMethods = stream(errorHandlerIndices)
            .mapToObj(index -> getHandlerMethod(index, method, parameters[index], Throwable.class))
            .toArray(Method[]::new);

        return objects -> invocationResult -> {

            final Consumer<Throwable> throwableConsumer = throwable -> {
                stream(errorHandlerIndices).forEach(i -> {
                    try {
                        errorHandlerMethods[i].invoke(objects[i], throwable);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        logger.error("Caught Exception passing Exception to Exception handler (It's confusing, I know, but trust me.)", ex);
                    }
                });
            };

            final Consumer<Object> objectConsumer = object -> {
                stream(resultHandlerIndices).forEach(i -> {
                    try {
                        resultHandlerMethods[i].invoke(objects[i], object);
                    } catch (InvocationTargetException ex) {
                        // Any issue that happens attempting to relay the result, this will forward the
                        // exception along.
                        logger.error("Caught exception invoking result acceptor.  Forwarding invocation target exception.", ex);
                        throwableConsumer.accept(ex.getTargetException());
                    } catch (IllegalAccessException ex) {
                        // Any issue that happens attempting to relay the result, this will forward the
                        // exception along.
                        logger.error("Caught exception invoking result acceptor.", ex);
                        throwableConsumer.accept(ex);
                    }
                });
            };

            if (invocationResult.isOk()) {
                objectConsumer.accept(invocationResult.getResult());
            } else {
                throwableConsumer.accept(invocationResult.getThrowable());
            }

        };

    }

    private Method getHandlerMethod(final int index,
                                    final Method method,
                                    final Parameter parameter,
                                    final Class<?> paramterType) {

        final Class<?> type = parameter.getType();

        if (type.getAnnotation(FunctionalInterface.class) == null) {

            final String msg = "Parameter at index " + index + " " +
                               " in method " + format(method) +
                               " is not annotated with @FunctionalInterface";

            throw new IllegalArgumentException(msg);

        }

        final Method handlerMethod = methods(type)
            .filter(m -> !m.isDefault())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No non-default method found in type: " + type));

        if (handlerMethod.getParameterCount() != 1) {
            final String msg = format(handlerMethod) + " must accept a single parameter.";
            throw new IllegalArgumentException(msg);
        }

        final Parameter handlerParameter = handlerMethod.getParameters()[0];

        if (!handlerParameter.getType().isAssignableFrom(Throwable.class)) {
            final String msg = format(handlerMethod) + " must accept a Throwable.";
            throw new IllegalArgumentException(msg);
        }

        return handlerMethod;

    }

    /**
     * Transforms the return value as the result of an {@link Invocation}.
     */
    @FunctionalInterface
    private interface ReturnValueTransformer {

        /**
         * Performs the translation.  This will translate the return value and, if necessary, throw an instance of
         * {@link Throwable} if the remote {@link Method} failed.
         *
         * @param objectFuture the {@link Future<Object>} supplied by {@link RemoteInvoker#invoke(Invocation, Consumer)}
         * @return an {@link Object} to return from the {@link InvocationHandler}
         * @throws Throwable if an exception occurs, can also be re-throwing the remiote invocation error
         */
        Object transform(Future<Object> objectFuture) throws Throwable;

    }

    /**
     * Transforms the object paramters to {@link Consumer<InvocationResult>} to be handed into the {@link RemoteInvoker}
     * to relay network errors.
     */
    @FunctionalInterface
    private interface InvocationConsumerTransformer {

        /**
         * Handles the invocation and returns the appropriate {@link Consumer<InvocationResult>} which will handle
         * the results.
         *
         * @param args the args
         * @return the {@link Consumer<InvocationResult>}
         */
        Consumer<InvocationResult> handle(Object[] args);

    }

}
