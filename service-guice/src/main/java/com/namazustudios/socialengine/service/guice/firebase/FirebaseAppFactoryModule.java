package com.namazustudios.socialengine.service.guice.firebase;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.firebase.CachingFirebaseAppFactory;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactory;

public class FirebaseAppFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FirebaseAppFactory.class).to(CachingFirebaseAppFactory.class).asEagerSingleton();
    }

}
