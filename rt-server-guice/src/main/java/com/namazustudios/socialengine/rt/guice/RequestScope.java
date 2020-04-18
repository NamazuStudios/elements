package com.namazustudios.socialengine.rt.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.Attributes;
import com.namazustudios.socialengine.rt.MutableAttributes;
import com.namazustudios.socialengine.rt.Request;

import static com.google.inject.Scopes.isCircularProxy;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * A custom {@link Scope} used for the live of a single {@link Request}.  Objects will be cached in the
 * {@link Attributes} of a {@link Request} as needed.
 */
public class RequestScope implements Scope {

    private static final RequestScope instance = new RequestScope();

    public static final Key<Request> CURRENT_REQUEST = Key.get(Request.class);

    public static RequestScope getInstance() {
        return instance;
    }

    private static final Request proxy = (Request) newProxyInstance(
        getSystemClassLoader(),
        new Class[]{Request.class}, (proxy, method, args) -> {
            final Request actual = instance.getCurrentRequest();
            return method.invoke(actual, args);
        });

    private final ThreadLocal<StackContext> current = new ThreadLocal<>();

    private RequestScope() {}

    @Override
    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
        return CURRENT_REQUEST.equals(key) ? () -> (T) proxy : requestAttributeProvider(key, unscoped);
    }

    private <T> Provider<T> requestAttributeProvider(final Key<T> key, final Provider<T> unscoped) {
        return () -> {
            final Request request = getCurrentRequest();
            final MutableAttributes attributes = request.getAttributes();
            return (T) attributes.getAttribute(key.toString()).orElseGet(() -> {
                final T object = unscoped.get();
                if (!isCircularProxy(object)) attributes.setAttribute(key.toString(), object);
                return object;
            });
        };
    }

    private Request getCurrentRequest() {
        final Context context = current.get();
        if (context == null) throw new IllegalStateException("Not in scope.");
        return context.getRequest();
    }

    public void ensureEmpty() {
        if (current.get() != null) throw new IllegalStateException("Currently in request scope.  Expecting empty scope.");
    }

    public Context enter(final Request request) {

        if (request == proxy) throw new IllegalArgumentException("Cannot use proxy Request.");

        final StackContext existing = current.get();

        if (existing == null) {
            final StackContext context = new StackContext(request);
            current.set(context);
            return context;
        } else if (request != null) {
            return existing.push(request);
        } else {
            throw new IllegalArgumentException("request must not be null!");
        }

    }

    /**
     * Makes the {@link Request} {@link Inject}able.
     *
     * @param binder the {@link Binder} to use
     */
    public void bind(final Binder binder) {
        binder.bind(Request.class).toInstance(proxy);
    }

    /**
     * Makes the {@link Request} {@link Inject}able as well as uses {@link PrivateBinder#expose(Key)} to ensure it is
     * available to the whole {@link Injector}.
     *
     * @param binder the {@link Binder} to use
     */
    public void bind(final PrivateBinder binder) {
        bind((Binder)binder);
        binder.expose(Request.class);
    }

    public interface Context extends AutoCloseable {

        @Override
        void close();

        Request getRequest();

    }

    private class StackContext implements Context {

        private StackContext next;

        private StackContext prev;

        private final Request request;

        public StackContext(final Request request) {
            this.request = request;
        }

        @Override
        public Request getRequest() {
            return request;
        }

        public StackContext push(final Request request) {
            final StackContext context = new StackContext(request);
            context.prev = this;
            current.set(next = context);
            return context;
        }

        @Override
        public void close() {
            if (next != null) next.prev = prev;
            if (prev != null) prev.next = next;
            if (current.get() == this) current.set(prev);
            if (next == null && prev == null) current.set(null);
        }

    }


}
