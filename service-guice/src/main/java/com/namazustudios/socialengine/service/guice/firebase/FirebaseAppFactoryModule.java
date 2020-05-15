package com.namazustudios.socialengine.service.guice.firebase;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactory;
import com.namazustudios.socialengine.service.firebase.FirebaseAppFactoryProvider;

public class FirebaseAppFactoryModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(FirebaseAppFactory.class).toProvider(FirebaseAppFactoryProvider.class);
    }

}
