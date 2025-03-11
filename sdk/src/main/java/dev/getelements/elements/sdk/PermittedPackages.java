package dev.getelements.elements.sdk;

import java.util.function.Predicate;

/**
 * Allows application code to specify a list of permitted packages which will be visible to {@link Element} instances
 * when loaded with the {@link ElementType#ISOLATED_CLASSPATH}.
 *
 * This inherits from {@link Predicate} which will receive the fully-qualified name of the {@link Package}, returning
 * true if the package is allowed exposed to {@link Element} instances with {@link ElementType#ISOLATED_CLASSPATH} type.
 */
public interface PermittedPackages extends Predicate<Package> {}
