package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class AppNodeSecurityModule extends PrivateModule {

    @Override
    protected void configure() {

        final var userAuthenticationMethodMultibinder = newSetBinder(binder(), UserAuthenticationMethod.class);
        final var profileIdentificationMethodMultibinder = newSetBinder(binder(), ProfileIdentificationMethod.class);

    }

}
