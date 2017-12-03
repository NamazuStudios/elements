package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.remote.InvocationResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
         * The method is dispatched, blocking until the method returns, and the return value is passed to the
         * {@link InvocationResult#setResult(Object)}.  If the invocation throws an exception, then the exception is
         * passed to {@link InvocationResult#setThrowable(Throwable)}, false is {@link InvocationResult#setOk(boolean)}.
         *
         * This mode is automatically selected if a {@link Method} returns an object and neither
         * {@link ResultHandler} or {@link ErrorHandler} are specified in the parameters.
         */
        SYNCHRONOUS,

        /**
         * This behaves exactly like {@link #SYNCHRONOUS}, except the calling method must return some type of
         * {@link Future<?>}.  The result is obtained through {@link Future#get()}, or {@link ExecutionException}
         * in the event of a failure.
         */
        FUTURE_SYNCHRONOUS,

        /**
         * The method is dispatched and the return value is discarded.  The value received by the {@link ResultHandler}
         * annotated object will be handed  to {@link InvocationResult#setResult(Object)}.  The value passed to the
         * {@link ErrorHandler} will be handed to {@link InvocationResult#setThrowable(Throwable)} and the flag set to
         * false {@link InvocationResult#setOk(boolean)}.
         */
        ASYNCHRONOUS

    }

}
