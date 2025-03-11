package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.annotation.ElementDefinition;

/**
 * Specifies the type of the {@link Element}, which has largely to do with its loading semantics.
 */
public enum ElementType {

    /**
     * The standard default type. Comes from specific {@link ClassLoader} and is isolated. Only one
     * {@link ElementDefinition} may exist in the classpath of the {@link Element} to support proper isolation and will
     * cause an exception at load time when trying to build one.
     *
     * {@link ElementLoaderFactory#getIsolatedLoader(Attributes, ClassLoader, ElementLoaderFactory.ClassLoaderConstructor)}
     */
    ISOLATED_CLASSPATH,

    /**
     * A type which is typically from the main application's {@link ClassLoader} (usually the System ClassLoader) where
     * multiple {@link ElementDefinition} may exist
     */
    SHARED_CLASSPATH

}
