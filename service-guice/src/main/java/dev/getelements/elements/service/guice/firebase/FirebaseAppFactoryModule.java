package dev.getelements.elements.service.guice.firebase;

import com.google.inject.AbstractModule;
import dev.getelements.elements.service.firebase.FirebaseAppFactory;
import dev.getelements.elements.service.firebase.CachingFirebaseAppFactory;

public class FirebaseAppFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FirebaseAppFactory.class).to(CachingFirebaseAppFactory.class).asEagerSingleton();
    }

}
