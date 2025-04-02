package dev.getelements.elements.rt.guice;

import com.google.inject.*;
import dev.getelements.elements.rt.CurrentResource;
import dev.getelements.elements.rt.Resource;
import dev.getelements.elements.sdk.guice.ReentrantThreadLocalScope;

/**
 * A Guice {@link Scope} which tracks the current {@link Resource}. It relies on {@link CurrentResource} as the source
 * of the current {@link Resource}.
 */
public class ResourceScope {

    private ResourceScope() {}

    private static final ReentrantThreadLocalScope<Resource> instance;

    static {
        instance = new ReentrantThreadLocalScope<>(
            Resource.class,
            CurrentResource.getInstance(),
            Resource::getAttributes
        );
    }

    /**
     * Gets the static instance of the {@link Scope}
     *
     * @return the {@link Scope}
     */
    public static Scope getInstance() {
        return instance;
    }

    /**
     * Makes the {@link Resource} {@link Inject}able.
     *
     * @param binder the {@link Binder} to use
     */
    public static void bind(final Binder binder) {
        binder.bind(Resource.class).toInstance(instance.getProxy());
    }

    /**
     * Makes the {@link Resource} {@link Inject}able as well as uses {@link PrivateBinder#expose(Key)} to ensure it is
     * available to the whole {@link Injector}.
     *
     * @param binder the {@link Binder} to use
     */
    public static void bind(final PrivateBinder binder) {
        bind((Binder)binder);
        binder.expose(Resource.class);
    }

}
