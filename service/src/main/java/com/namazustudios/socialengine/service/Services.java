package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotImplementedException;

import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * A class which helps implement the {@link javax.inject.Provider} instances
 * for the service-level objects.  This provides default implementations designed
 * to throw exceptions, effectively blocking usage of the service.
 *
 * Created by patricktwohig on 4/2/15.
 */
public class Services {

    private static final ConcurrentMap<Class<?>, Object> forbiddenServices = new ConcurrentHashMap<>();

    /**
     * Gets a proxy that always throws an instance of {@link ForbiddenException}.  This uses the {@link Proxy}
     * method builtin to the JDK.  As such, this will not work properly with non-interface types.
     *
     * @param cls the service type {@link Class}.
     * @param <T> the service proxy instance.
     *
     * @return a proxy instance of the service type
     */
    public static <T> T forbidden(final Class<T> cls) {
        return (T) forbiddenServices.computeIfAbsent(cls, c ->
            newProxyInstance(Services.class.getClassLoader(), new Class[]{c}, (p,m,a) -> {
                throw new ForbiddenException("user not logged in or credentials not supplied");
            }
        ));
    }

    private static final ConcurrentMap<Class<?>, Object> unimplementedServices = new ConcurrentHashMap<>();

    /**
     * Gets a proxy that always throws an instance of {@link NotImplementedException}.  This uses the {@link Proxy}
     * method builtin to the JDK.  As such, this will not work properly with non-interface types.
     *
     * @param cls the service type {@link Class}.
     * @param <T> the service proxy instance.
     *
     * @return a proxy instance of the service type
     */
    public static <T> T unimplemented(final Class<T> cls) {
        return (T) unimplementedServices.computeIfAbsent(cls, c ->
                newProxyInstance(Services.class.getClassLoader(), new Class[]{c}, (p,m,a) -> {
                            throw new NotImplementedException("not implemented");
                        }
                ));
    }

}
