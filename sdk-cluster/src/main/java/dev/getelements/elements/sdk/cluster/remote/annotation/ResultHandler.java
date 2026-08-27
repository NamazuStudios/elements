package dev.getelements.elements.sdk.cluster.remote.annotation;

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
 * This can be used with type such as {@link Consumer} or {@link Function}, essentially any {@link FunctionalInterface}
 * will get mapped over the remote/network call.
 *
 * If there exists either {@link ResultHandler} or {@link ErrorHandler}, this method's return type will not be used and
 * this should result in an error
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResultHandler {}
