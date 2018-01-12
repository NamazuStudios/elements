package com.namazustudios.socialengine.rt.annotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.Reflection.format;
import static java.util.Arrays.stream;

/**
 * This annotation specifies the dispatching strategy for the method.  This annotation is optional and if the method
 * does not contain this annotation, then the dispatching is determined automatically based on the presence, or lack of,
 * {@link ResultHandler} or {@link ErrorHandler}, and method return types.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dispatch {

    /**
     * Specifies the {@link Type} for the method.
     *
     * @return the dispatch type
     */
    Type value();

    /**
     * The dispatch type.
     */
    enum Type {

        /**
         * The method is dispatched, blocking until the method returns or an exception is thrown.
         *
         * This mode is automatically selected if a {@link Method} returns an object and neither
         * {@link ResultHandler} or {@link ErrorHandler} are specified in the parameters.  This implies that the return
         * type must be serializable.  The invocation will block until the remote end produces a result.
         */
        SYNCHRONOUS,

        /**
         * This behaves similar to {@link #SYNCHRONOUS}, except the calling method must return some type of
         * {@link Future<?>}.  The result is obtained through {@link Future#get()}, or {@link ExecutionException}
         * in the event of a failure.
         *
         *
         * The method may use {@link ResultHandler} or {@link ErrorHandler} to receive results in addition to the
         * returned {@link Future}.
         *
         * On the client side, either {@link ResultHandler} or {@link ErrorHandler} may be used to receive the result.
         * The client method invocation will not block and instead return an instance of {@link Future} which will be
         * used to dispatch the remote invocation.
         */
        FUTURE,

        /**
         * The method is dispatched and the return value is discarded on the remote end.  The annotated {@link Method}
         * should be a void method.  However, null will be substituted on the local side for any return value.
         * Primitives are not supported.
         *
         * The method must use {@link ResultHandler} and {@link ErrorHandler} to receive results.  On both client and
         * server side.
         *
         * The method may return a value.  However, this will indicate that the client side code will block until the
         * remote method has returned.  If the method returns a {@link Future}, then it will be returned without
         * blocking.  If the method return type is void, then no blocking will happen.
         *
         */
        CONSUMER;

        /**
         * Inspects the supplied {@link Method} and determines the type of dispatch used.  If not specified, then this
         * method infers the type automatically.
         *
         * @param method
         * @return the {@link Type}
         */
        public static Type determine(final Method method) {
            final Dispatch dispatch = method.getAnnotation(Dispatch.class);
            return dispatch == null ? infer(method) : dispatch.value();
        }

        private static Type infer(final Method method) {

            final Function<Class<? extends Annotation>, Integer> count = aClass -> (int) stream(method.getParameters())
                .map(p -> p.getAnnotation(aClass))
                .filter(a -> aClass.isInstance(a))
                .count();

            final int errorCount = count.apply(ErrorHandler.class);
            final int resultCount = count.apply(ResultHandler.class);

            if (Future.class.isAssignableFrom(method.getReturnType())) {
                return FUTURE;
            } if (errorCount == 0 && resultCount == 0) {
                return SYNCHRONOUS;
            } else if (errorCount != 1) {
                final String msg = String.format("Only one of %s can be specified for %s", ErrorHandler.class.getSimpleName(), format(method));
                throw new IllegalArgumentException(msg);
            } else {
                return CONSUMER;
            }

        }

    }

}
