package dev.getelements.elements.deployment.jetty.loader;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

/**
 * A Jetty {@link Handler.Wrapper} that switches the thread context classloader (TCCL) to the
 * element's {@link ClassLoader} for the duration of each request, restoring the original
 * classloader in a {@code finally} block.
 *
 * <p>This is required for element-hosted services that share the platform Morphia
 * {@code Datastore}. Morphia's {@code DiscriminatorLookup} falls back to
 * {@code Class.forName(discriminatorValue)} when an entity class is not pre-registered; that
 * call resolves classes using the calling thread's context classloader. Without this wrapper it
 * would use the Jetty thread pool's classloader (the platform classloader), making
 * element-specific entity classes invisible during document decoding.
 *
 * <p>Placing this wrapper outside the {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler}
 * ensures the TCCL is set before Jetty's own context scope handling, and also covers any
 * non-servlet code paths (e.g. async dispatch, error handling) that Jetty may route through
 * the handler chain without re-entering the servlet context.
 */
class ClassLoaderSwitchHandler extends Handler.Wrapper {

    private final ClassLoader classLoader;

    ClassLoaderSwitchHandler(final ClassLoader classLoader, final Handler handler) {
        super(handler);
        this.classLoader = classLoader;
    }

    @Override
    public boolean handle(final Request request, final Response response, final Callback callback)
            throws Exception {
        final var thread = Thread.currentThread();
        final var previous = thread.getContextClassLoader();
        try {
            thread.setContextClassLoader(classLoader);
            return super.handle(request, response, callback);
        } finally {
            thread.setContextClassLoader(previous);
        }
    }

}
