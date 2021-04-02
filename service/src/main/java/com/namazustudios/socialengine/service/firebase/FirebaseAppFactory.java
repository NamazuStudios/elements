package com.namazustudios.socialengine.service.firebase;

import com.google.firebase.FirebaseApp;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

/**
 * Creates instances of {@link FirebaseApp} from a variety of sources. This ideally should implement a caching strategy
 * in order to avoid creating unnecessary instances of {@link FirebaseApp} as said instances are fairly heavyweight
 * instances.
 */
public interface FirebaseAppFactory {

    /**
     * Loads an instance of {@link FirebaseApp} with the supplied {@link Application}.
     *
     * @param application the {@link Application}
     * @return the {@link FirebaseApp}
     */
    FirebaseApp fromApplication(Application application);

    /**
     * Loads the {@link FirebaseApp} from the supplied {@link FirebaseApplicationConfiguration}.
     *
     * @param firebaseApplicationConfiguration the {@link FirebaseApplicationConfiguration}
     * @return the {@link FirebaseApp}
     */
    FirebaseApp fromConfiguration(FirebaseApplicationConfiguration firebaseApplicationConfiguration);

}
