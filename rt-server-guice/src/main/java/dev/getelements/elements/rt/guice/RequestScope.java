package dev.getelements.elements.rt.guice;

import com.google.inject.*;
import dev.getelements.elements.rt.*;

/**
 * A custom {@link Scope} used for the live of a single {@link Request}.  Objects will be cached in the
 * {@link Attributes} of a {@link Request} as needed.
 */
public class RequestScope {

    private RequestScope() {}

    private static final ReentrantThreadLocalScope<Request> instance;

    static {
        instance = new ReentrantThreadLocalScope<>(Request.class, CurrentRequest.getInstance(), Request::getAttributes);
    }

    public static Scope getInstance() {
        return instance;
    }

    /**
     * Makes the {@link Request} {@link Inject}able.
     *
     * @param binder the {@link Binder} to use
     */
    public static void bind(final Binder binder) {
        binder.bind(Request.class).toInstance(instance.getProxy());
    }

    /**
     * Makes the {@link Request} {@link Inject}able as well as uses {@link PrivateBinder#expose(Key)} to ensure it is
     * available to the whole {@link Injector}.
     *
     * @param binder the {@link Binder} to use
     */
    public static void bind(final PrivateBinder binder) {
        bind((Binder)binder);
        binder.expose(Request.class);
    }

}
