package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Reflection;
import com.namazustudios.socialengine.rt.annotation.Dispatch;
import com.namazustudios.socialengine.rt.annotation.ErrorHandler;
import com.namazustudios.socialengine.rt.annotation.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.namazustudios.socialengine.rt.Reflection.methods;

/**
 * Inspects the attributes, parameters, and annotations of a specific {@link Method} to build an instance of
 * {@link InvocationDispatcher} to dispatch {@link Invocation} instances to a local object in memory.
 */
public class LocalInvocationDispatcherBuilder {

    private static final Logger logger = LoggerFactory.getLogger(LocalInvocationDispatcherBuilder.class);

    private final Method method;

    private final Dispatch.Type dispatchType;

    public LocalInvocationDispatcherBuilder(
            final Class<?> type,
            final String name,
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

        final Function<List<Object>, Object[]> parametersTransformer;
        parametersTransformer = getParametersTransformer();

        final Method method = getMethod();

        return (target, invocation, invocationErrorConsumer, invocationReturnConsumer, invocationResultConsumerList) -> {

            final Object[] args = parametersTransformer.apply(invocation.getArguments());

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
                // This should not happen because we only consider public methods in the binding
                logger.error("IllegalAccessException dispatching method", ex);
                final InvocationResult invocationResult = new InvocationResult();
                final InvocationError invocationError = new InvocationError();
                invocationError.setThrowable(ex);
                invocationErrorConsumer.accept(invocationError);
            }

        };

    }

    private Function<List<Object>, Object[]> getParametersTransformer() {

        final Method method = getMethod();
        final Parameter[] parameters = method.getParameters();

        final int argCount = method.getParameterCount();

        final int resultHandlerIndex = IntStream.range(0, parameters.length)
            .filter(i -> parameters[i].getAnnotation(ResultHandler.class) != null)
            .findFirst().orElse(-1);

        final int errorHandlerIndex = IntStream.range(0, parameters.length)
            .filter(i -> parameters[i].getAnnotation(ErrorHandler.class) != null)
            .findFirst().orElse(-1);

        return objectList -> {
            final Object[] args = new Object[argCount];
            return args;
        };

    }

}
