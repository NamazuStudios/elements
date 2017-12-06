package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Reflection;
import com.namazustudios.socialengine.rt.annotation.Dispatch;
import com.namazustudios.socialengine.rt.annotation.ResultHandler;
import com.namazustudios.socialengine.rt.annotation.Serialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.Reflection.*;
import static java.util.Arrays.stream;

/**
 * Inspects the attributes, parameters, and annotations of a specific {@link Method} to build an instance of
 * {@link InvocationDispatcher} to dispatch {@link Invocation} instances to a local object in memory.
 */
public class LocalInvocationDispatcherBuilder {

    private static final Logger logger = LoggerFactory.getLogger(LocalInvocationDispatcherBuilder.class);

    private final Method method;

    private final Dispatch.Type dispatchType;

    public LocalInvocationDispatcherBuilder(
            final Class<?> type, final String name,
            final List<String> parameters) throws ClassNotFoundException {

        final List<Class<?>> parameterTypes = new ArrayList<>();

        for(final String parameter : parameters) {
            parameterTypes.add(Class.forName(parameter));
        }

        this.method = methods(type).filter(m -> m.getName().equals(name))
                                   .filter(m -> m.getParameterTypes().equals(parameterTypes))
                                   .findFirst().orElseThrow(() -> Reflection.noSuchMethod(type, name, parameterTypes));

        this.dispatchType = Dispatch.Type.determine(method);

    }

    /**
     * Gets the {@link Method} to dispatch.
     *
     * @return the {@link Method}
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the {@link Dispatch.Type} strategy to use when dispatching the method.
     *
     * @return the {@link }
     */
    public Dispatch.Type getDispatchType() {
        return dispatchType;
    }

    /**
     * Builds a new instance of the {@link InvocationDispatcher}.
     *
     * @return returns the {@link InvocationDispatcher}
     */
    public LocalInvocationDispatcher build() {

        final BiConsumer<List<Object>, Object[]> parametersTransformer;
        parametersTransformer = getParametersTransformer();

        final BiConsumer<Consumer<InvocationError>, Object[]> errorHandlerTransformer;
        errorHandlerTransformer = getErrorHandlerTransformer();

        final BiConsumer<List<Consumer<InvocationResult>>, Object[]> resultHandlerTransformer;
        resultHandlerTransformer = getResultHandlerTransformer();

        final Method method = getMethod();
        final int argCount = method.getParameterCount();

        return (target, invocation, invocationErrorConsumer, invocationReturnConsumer, invocationResultConsumerList) -> {

            final Object[] args = new Object[argCount];

            parametersTransformer.accept(invocation.getArguments(), args);
            errorHandlerTransformer.accept(invocationErrorConsumer, args);
            resultHandlerTransformer.accept(invocationResultConsumerList, args);

            try {
                final Object returnValue = method.invoke(target, args);
                final InvocationResult invocationResult = new InvocationResult();
                invocationResult.setResult(returnValue);
                invocationReturnConsumer.accept(invocationResult);
            } catch (InvocationTargetException ex) {
                logger.info("Caught exception dispatching the respnse.", ex);
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(ex.getTargetException());
                invocationErrorConsumer.accept(invocationError);
            } catch (IllegalAccessException ex) {
                // This should not happen because we only consider public methods in the binding but we need to catch
                // it anyhow and try to relay it to the client.
                logger.error("IllegalAccessException dispatching method", ex);
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(ex);
                invocationErrorConsumer.accept(invocationError);
            }

        };

    }

    private BiConsumer<List<Object>, Object[]> getParametersTransformer() {

        final Method method = getMethod();
        final int serializeIndices[] = indices(method, Serialize.class);

        return (objectList, args) -> {
            final Iterator<Object> argIterator = objectList.iterator();
            stream(serializeIndices).forEach(index -> args[index] = argIterator.next());
        };

    }

    private BiConsumer<Consumer<InvocationError>, Object[]> getErrorHandlerTransformer() {

        final Method method = getMethod();
        final int errorHandlerIndex = errorHandlerIndex(method);

        return (invocationErrorConsumer, args) -> {
            final Object errorHandler = proxyErrorHandler(errorHandlerIndex, invocationErrorConsumer);
            args[errorHandlerIndex] = errorHandler;
        };

    }

    private Object proxyErrorHandler(final int errorHandlerIndex,
                                     final Consumer<InvocationError> invocationErrorConsumer) {

        final Method method = getHandlerMethod(getMethod().getParameters()[errorHandlerIndex]);

        return new ProxyBuilder<>(method.getDeclaringClass())
            .withToString()
            .withDefaultHashCodeAndEquals()
            .withSharedMethodHandleCache()
            .handler((proxy, m, args) -> {
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable((Throwable) args[0]);
                invocationErrorConsumer.accept(invocationError);
                return null;
            }).forMethod(method);

    }

    private BiConsumer<List<Consumer<InvocationResult>>, Object[]> getResultHandlerTransformer() {

        final Method method = getMethod();
        final int resultHandlerIndices[] = indices(method, ResultHandler.class);

        return (invocationResultConsumerList, args) -> {

            final Iterator<Consumer<InvocationResult>> invocationResultIterator;
            invocationResultIterator = invocationResultConsumerList.iterator();

            stream(resultHandlerIndices).forEach(index -> {
                final Object resultHandler = proxyResultHandler(index, invocationResultIterator.next());
                args[index] = resultHandler;
            });

        };

    }

    private Object proxyResultHandler(final int index, final Consumer<InvocationResult> invocationResultConsumer) {

        final Method method = getHandlerMethod(getMethod().getParameters()[index]);

        return new ProxyBuilder<>(method.getDeclaringClass())
            .withToString()
            .withDefaultHashCodeAndEquals()
            .withSharedMethodHandleCache()
            .handler((proxy, m, args) -> {
                final InvocationResult invocationResult = new InvocationResult();
                invocationResult.setResult(args[0]);
                invocationResultConsumer.accept(invocationResult);
                return null;
            }).forMethod(method);

    }

}
