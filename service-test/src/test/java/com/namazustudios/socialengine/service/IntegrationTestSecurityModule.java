package com.namazustudios.socialengine.service;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.security.ProfileSupplierProvider;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;
import com.namazustudios.socialengine.security.UserProvider;

import java.util.function.Supplier;

public class IntegrationTestSecurityModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(User.class).toProvider(UserProvider.class);
        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);

        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(IntegrationTestContextUserAuthenticationMethod.class);

        final Multibinder<ProfileIdentificationMethod>profileIdentificationMethodMultibinder;
        profileIdentificationMethodMultibinder = Multibinder.newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(IntegrationTestProfileIdentificationMethod.class);

        bind(TestScope.Context.class).toProvider(TestScope::current);

    }
}
