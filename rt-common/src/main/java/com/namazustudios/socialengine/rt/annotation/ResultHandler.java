package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Designates a {@link FunctionalInterface} annotated-type parameter which will receive the result returned from a
 * remote invocation.  The targeted parameter may be any functional type which accepts a single argument.  The return
 * value, if applicable, is not used.
 *
 * This can be used with type such as {@link Consumer<?>} or {@link Function<?, ?>}.
 *
 * If there exists either {@link ResultHandler} or {@link ErrorHandler}, the method is assumed to be asynchronous and
 * the return value of the method will not be used
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultHandler {}
