package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.Reflection;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.namazustudios.socialengine.rt.Reflection.methods;

/**
 * Inspects the attributes, parameters, and annotations of a specific {@link Method} to build an instance of
 * {@link InvocationDispatcher} to dispatch {@link Invocation} instances to a local object in memory.
 */
public class LocalInvocationDispatcherBuilder {

    private final Method method;

    public LocalInvocationDispatcherBuilder(
            final Class<?> type,
            final String name,
            final List<String> parameters) throws ClassNotFoundException {

        final List<Class<?>> parameterTypes = new ArrayList<>();

        for(final String parameter : parameters) {
            parameterTypes.add(Class.forName(parameter));
        }

        this.method = methods(type).filter(m -> m.getName().equals(name))
                                   .filter(m -> m.getParameterTypes().equals(parameterTypes))
                                   .findFirst().orElseThrow(() -> Reflection.noSuchMethod(type, name, parameterTypes));

    }

    /**
     * Builds a new instance of the {@link InvocationDispatcher}.
     *
     * @return returns the {@link InvocationDispatcher}
     */
    public InvocationDispatcher build() {
        return null;
    }

}
