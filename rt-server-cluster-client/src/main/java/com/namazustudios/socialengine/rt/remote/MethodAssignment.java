package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;

/**
 * Assigns the previous action to a particular {@link Method}.
 */
public interface MethodAssignment<NextT> {

    NextT forMethod(final String name);

    NextT forMethod(final String name, final Class<?>... args);

    NextT forMethod(final Method method);

}
