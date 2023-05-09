package dev.getelements.elements.rt.testkit;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * Used to return a mock that is unimplemented.  This relies on the underlying {@link Proxy} service built into the VM,
 * therefore it only supports interface types.
 */
public class UnimplementedMock {

    private static final ConcurrentMap<Class<?>, Object> unimplemented = new ConcurrentHashMap<>();

    /**
     * Gets a proxy that always throws an instance of {@link UnsupportedOperationException}.  This uses the {@link Proxy}
     * method builtin to the JDK.  As such, this will not work properly with non-interface types.
     *
     * @param cls the type {@link Class}.
     * @param <T> the service proxy instance.
     *
     * @return a proxy instance of the service type
     */
    public static <T> T unimplemented(final Class<T> cls) {
        return (T) unimplemented.computeIfAbsent(cls, c ->
            newProxyInstance(UnimplementedMock.class.getClassLoader(), new Class[]{c}, (p, m, a) -> {
                        throw new UnsupportedOperationException("Not Implemented In Unit Test.");
                    }
            ));
    }

}
