package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.Observation;
import com.namazustudios.socialengine.rt.handler.HandlerClientSessionObserver;
import com.namazustudios.socialengine.rt.handler.Session;
import com.namazustudios.socialengine.rt.util.LazyValue;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.UUID.randomUUID;

public class ServletSession implements Session {

    private final LazyValue<HttpSession> httpSessionLazyValue;

    private final Function<HandlerClientSessionObserver, Observation> observeIdleFunction;

    private final Function<HandlerClientSessionObserver, Observation> observeDisconnectFunction;

    private final Map<Object, String> keyMap = new HashMap<>();

    private final Object lock = new Object();

    public ServletSession(final Supplier<HttpSession> httpSessionSupplier,
                          final Function<HandlerClientSessionObserver, Observation> observeIdleFunction,
                          final Function<HandlerClientSessionObserver, Observation> observeDisconnectFunction) {
        this.httpSessionLazyValue = new LazyValue<>(httpSessionSupplier);
        this.observeIdleFunction = observeIdleFunction;
        this.observeDisconnectFunction = observeDisconnectFunction;
    }

    @Override
    public String getId() {
        synchronized (lock) {
            return httpSessionLazyValue.get().getId();
        }
    }

    @Override
    public void setSessionVariable(Object key, Object value) {
        synchronized (lock) {
            final String name = keyMap.computeIfAbsent(key, this::generateName);
            httpSessionLazyValue.get().setAttribute(name, value);
        }
    }

    @Override
    public Object setSessionVariableIfAbsent(Object key, Object value) {
        synchronized (lock) {

            final String newName = generateName(key);
            final String existingName = keyMap.putIfAbsent(key, newName);

            final HttpSession httpSession = httpSessionLazyValue.get();

            if (existingName == null) {
                httpSession.setAttribute(newName, value);
                return null;
            } else {
                return httpSession.getAttribute(existingName);
            }

        }
    }

    private String generateName(final Object key) {
        return key + randomUUID().toString();
    }

    @Override
    public Object getSessionVariable(Object key, Object defaultValue) {
        final String name = keyMap.get(key);
        return name == defaultValue ? defaultValue : httpSessionLazyValue.get().getAttribute(name);
    }

    @Override
    public void removeSessionVariable(Object key) {

        final String name = keyMap.get(key);

        if (name != null) {
            httpSessionLazyValue.get().removeAttribute(name);
        }

    }

    @Override
    public Observation observeDisconnect(final HandlerClientSessionObserver handlerClientSessionObserver) {
        return observeIdleFunction.apply(handlerClientSessionObserver);
    }

    @Override
    public Observation observeIdle(final HandlerClientSessionObserver handlerClientSessionObserver) {
        return observeDisconnectFunction.apply(handlerClientSessionObserver);
    }

    @Override
    public void disconnect() {
        synchronized (lock) {
            httpSessionLazyValue.get().invalidate();
        }
    }

}
