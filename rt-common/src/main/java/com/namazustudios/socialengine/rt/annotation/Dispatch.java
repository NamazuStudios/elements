package com.namazustudios.socialengine.rt.annotation;

import com.namazustudios.socialengine.rt.remote.InvocationResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Future;

/**
 * This annotation specifies the dispatching strategy for the method.  This annotation is optional and if the method
 * does not contain this annotation, then the dispatching is determined automatically based on the presence, or lack of,
 * {@link ResultHandler} or {@link ErrorHandler}.  If either annotation is present or the method returns {@link Future}
 * of any type, then the dispatch is determined to be {@link Type#ASYNCHRONOUS}.  Otherwise it is determined to be
 * {@link Type#SYNCHRONOUS}.
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
         * The method is dispatched and the return value is passed to the {@link InvocationResult#setResult(Object)}
         * blocking until the method returns.  If {@link #value()} returns {@link #SYNCHRONOUS} and the method
         * parameters contain {@link ResultHandler}, {@link ErrorHandler}, or returns a {@link Future}
         * then an error will be raised.
         */
        SYNCHRONOUS,

        /**
         * The method is dispatched and the return value is discarded to the {@link InvocationResult#setResult(Object)} or,
         * if the method.  If {@link #value()} returns {@link #SYNCHRONOUS} and the method parameteres contain
         * {@link ResultHandler}, {@link ErrorHandler}, or returns a {@link Future} then an error will be raised.
         */
        ASYNCHRONOUS

    }

}
