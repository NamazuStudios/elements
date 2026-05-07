package dev.getelements.elements.dao.mongo.provider;

import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class MongoDatastoreProvider implements Provider<Datastore> {

    public static final String MAIN = "dev.getelements.elements.mongo.datastore.main";

    @Inject
    private Provider<AtomicReference<Datastore>> datastoreAtomicReference;

    @Override
    public Datastore get() {
        return datastoreAtomicReference.get().get();
    }


}
