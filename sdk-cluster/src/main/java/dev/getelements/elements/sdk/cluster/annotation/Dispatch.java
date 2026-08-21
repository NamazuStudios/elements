package dev.getelements.elements.rt.annotation;

import dev.getelements.elements.rt.remote.AsyncOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static dev.getelements.elements.rt.Reflection.format;
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
         * This mode is automatically selected if a {@link Method} returns an object and neither {@link ResultHandler}
         * or {@link ErrorHandler} are specified in the parameters.  This implies that the return type must be
         * serializable.  The invocation will block until the remote end produces a result and returns the value.
         */
        SYNCHRONOUS,

        /**
         * The method is dispatched, returning immediately and deferring the driving of results to a background process
         * Which will call the associated {@link ErrorHandler} and {@link ResultHandler} parameters.
         *
         * This mode is automatically selected if a {@link Method} returns void and has both a {@link ResultHandler}
         * and {@link ErrorHandler} specified in the parameters.  This implies that the return type must be
         * serializable.
         */
        ASYNCHRONOUS,

        /**
         * This behaves similar to {@link #SYNCHRONOUS}, except the calling method must return some type of
         * {@link Future<?>}.  The result is obtained through {@link Future#get()}, or {@link ExecutionException}
         * in the event of a failure.
         *
         * The method may use {@link ResultHandler} or {@link ErrorHandler} to receive results in addition to the
         * returned {@link Future}, and may be independent results.
         *
         * On the client side, either {@link ResultHandler} or {@link ErrorHandler} may be used to receive the result.
         * The client method invocation will not block and instead return an instance of {@link Future} which will be
         * used to dispatch the remote invocation.
         */
        FUTURE,

        /**
         * This is a type of method that doesn't exactly fit in the other categories.  Depending on the context, a
         * method of this type may pick one of the other strategies to do the invocation.  Underlying connection
         * details may affect the actual behavior of the method. Usage of this type is discouraged and will generate
         * warnings in logs.
         */
        HYBRID,

        /**
         * @deprecated Renamed to {@link #HYBRID}.
         */
        @Deprecated
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

            final Class<?> rType = method.getReturnType();
            final int errorCount = count.apply(ErrorHandler.class);
            final int resultCount = count.apply(ResultHandler.class);

            if (Future.class.isAssignableFrom(rType)) {
                return FUTURE;
            } if (errorCount == 0 && resultCount == 0) {
                return SYNCHRONOUS;
            } else if (isAsync(rType)) {
                return ASYNCHRONOUS;
            } else if (errorCount != 1) {
                final String msg = String.format("Only one of %s can be specified for %s", ErrorHandler.class.getSimpleName(), format(method));
                throw new IllegalArgumentException(msg);
            } else {
                return HYBRID;
            }

        }

        private static boolean isAsync(final Class<?> rType) {
            return void.class.equals(rType) ||
                   Void.class.equals(rType) ||
                   AsyncOperation.class.isAssignableFrom(rType);
        }

    }

}
