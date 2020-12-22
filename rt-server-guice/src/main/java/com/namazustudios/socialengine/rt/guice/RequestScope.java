package com.namazustudios.socialengine.rt.guice;

import com.google.inject.*;
import com.namazustudios.socialengine.rt.*;

import static com.google.inject.Scopes.isCircularProxy;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.reflect.Proxy.newProxyInstance;

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

    public static ReentrantThreadLocalScope<Request> getInstance() {
        return instance;
    }

    public static ReentrantThreadLocal.Context<Request> enter(final Request request) {
        return CurrentRequest.getInstance().enter(request);
    }

}
