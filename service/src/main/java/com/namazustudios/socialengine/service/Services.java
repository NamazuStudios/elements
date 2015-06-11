package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.ForbiddenException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A class which helps implement the {@link javax.inject.Provider} instances
 * for the service-level objects.
 *
 * Created by patricktwohig on 4/2/15.
 */
public class Services {

    private static final ConcurrentMap<Class<?>, Object> forbiddenServices = new ConcurrentHashMap<>();

    /**
     * Gets a proxy that always throws an instance of {@link ForbiddenException}.
     *
     * @param cls the service type
     * @param <T> the service type
     * @return a proxy instance of the service type
     */
    public static <T> T forbidden(Class<T> cls) {

        T existing = (T) forbiddenServices.get(cls);

        if (existing != null) {
            return existing;
        }

        final T toPut = newForbiddenService(cls);
        existing = (T) forbiddenServices.putIfAbsent(cls, toPut);

        return  existing == null ? toPut : existing;

    }

    private static <T> T newForbiddenService(Class<T> cls) {
        return (T)Proxy.newProxyInstance(Services.class.getClassLoader(), new Class<?>[]{cls}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                throw new ForbiddenException();
            }
        });
    }

}
