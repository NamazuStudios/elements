package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.security.*;
import com.namazustudios.socialengine.servlet.security.HttpRequestAttributeAuthenticationMethod;
import com.namazustudios.socialengine.servlet.security.HttpRequestAttributeProfileIdentificationMethod;
import com.namazustudios.socialengine.servlet.security.HttpSessionUserAuthenticationMethod;

import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(User.class).toProvider(UserProvider.class);
        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);

        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpRequestAttributeAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpSessionUserAuthenticationMethod.class);

        final Multibinder<ProfileIdentificationMethod>profileIdentificationMethodMultibinder;
        profileIdentificationMethodMultibinder = Multibinder.newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestAttributeProfileIdentificationMethod.class);

    }

}
