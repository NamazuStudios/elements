package dev.getelements.elements.sdk;

import java.lang.reflect.Type;
import java.util.function.Predicate;

/**
 * Allows application code to specify a list of permitted types which will be visible to {@link Element} instances
 * when loaded with the {@link ElementType#ISOLATED_CLASSPATH}.
 *
 * This inherits from {@link Predicate} which will receive the fully-qualified name of the {@link Type}, returning
 * true if the type is allowed exposed to {@link Element} instances with {@link ElementType#ISOLATED_CLASSPATH} type.
 *
 */
public interface PermittedTypes extends Predicate<Class<?>> {}
