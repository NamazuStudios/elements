package com.namazustudios.socialengine.rt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Designates a {@link FunctionalInterface} annotated-type parameter which will receive an exception thrown from a
 * remote invocation.  The targeted parameter may be any functional type which accepts a single argument.  The return
 * value, if applicable, is not used.
 *
 * The method must accept a {@link Throwable} because remote invocations may have many reasons why they fail beyond the
 * cause of the invocation itself.
 *
 * This can be used with type such as {@link Consumer<Throwable>} or {@link Function<Throwable, ?>}.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ErrorHandler {}
