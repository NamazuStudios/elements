package com.namazustudios.promotion.service;

import com.namazustudios.promotion.exception.ForbiddenException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by patricktwohig on 4/2/15.
 */
public class Services {

    private static final ConcurrentMap<Class<?>, Object> forbiddenServices = new ConcurrentHashMap<>();

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
