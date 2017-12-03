package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;

/**
 * Assigns the previous action to a particular {@link Method}.
 */
public interface MethodAssignment<NextT> {

    /**
     * Assigns to the {@link Method} by name.
     *
     * @param name the name of the {@link Method}
     *
     * @return the next element in the builder chain
     */
    NextT forMethod(final String name);

    /**
     * Assigns to the {@link Method} by name and parameters.
     *
     * @param name the name of the {@link Method}
     * @param args the arguments to match
     *
     * @return the next element in the builder chain
     */
    NextT forMethod(final String name, final Class<?>... args);

    /**
     * Assigns to the supplied {@link Method} by name and parameters.
     *
     * @return the next element in the builder chain
     */
    NextT forMethod(final Method method);

}
