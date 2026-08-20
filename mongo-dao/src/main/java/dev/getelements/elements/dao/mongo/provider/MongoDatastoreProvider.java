package dev.getelements.elements.dao.mongo.provider;

import dev.getelements.elements.sdk.mongo.provider.LiveDatastore;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by patricktwohig on 5/8/15.
 *
 * <p>Returns a live-delegating {@link Datastore} proxy (see {@link LiveDatastore}) rather than the
 * concrete instance, so a consumer that captures the injected {@link Datastore} in a singleton field
 * or constructor parameter is always safe to do so, even across a later Element register/unregister
 * that rebuilds the underlying {@link Datastore}/{@code Mapper}.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    public static final String MAIN = "dev.getelements.elements.mongo.datastore.main";

    @Inject
    private Provider<AtomicReference<Datastore>> datastoreAtomicReference;

    private volatile Datastore proxy;

    @Override
    public Datastore get() {
        var result = proxy;
        if (result == null) {
            synchronized (this) {
                result = proxy;
                if (result == null) {
                    proxy = result = LiveDatastore.wrap(datastoreAtomicReference.get());
                }
            }
        }
        return result;
    }

}
