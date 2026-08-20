package dev.getelements.elements.sdk.mongo.provider;

import dev.morphia.Datastore;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Wraps a {@link AtomicReference} of {@link Datastore} in a stable proxy that forwards every call to
 * whichever {@link Datastore} is live at the moment of the call, rather than the one live at the moment
 * of injection.
 *
 * <p>{@code MongoElementEntityRegistrar} (in {@code mongo-dao}) rebuilds the platform's shared
 * {@link Datastore}/{@code Mapper} from scratch and swaps it into a
 * single {@link AtomicReference} every time an Element registers or unregisters entity classes. A
 * consumer that captures the concrete {@link Datastore} directly — e.g. a raw constructor/field
 * injection into an {@code asEagerSingleton()} binding — freezes whatever was live at that moment and
 * never observes a later rebuild, which can leave it holding {@code EntityModel}/{@code PropertyModel}
 * state tied to a since-superseded {@link ClassLoader}. Handing out this proxy instead means a captured
 * reference is never actually a snapshot, so it's always safe to hold in a singleton.
 */
public final class LiveDatastore {

    private LiveDatastore() {}

    /**
     * Wraps the given reference in a live-delegating {@link Datastore} proxy.
     *
     * @param ref the reference to the live {@link Datastore}, kept current elsewhere (e.g. by
     *            {@code MongoElementEntityRegistrar})
     * @return a {@link Datastore} that forwards every call to {@code ref.get()} at call time
     */
    public static Datastore wrap(final AtomicReference<Datastore> ref) {
        return (Datastore) Proxy.newProxyInstance(
                Datastore.class.getClassLoader(),
                new Class<?>[]{Datastore.class},
                (proxy, method, args) -> {
                    try {
                        return method.invoke(ref.get(), args);
                    } catch (InvocationTargetException ex) {
                        throw ex.getCause();
                    }
                }
        );
    }

}
