package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.ProfileIdentificationMethod;
import dev.getelements.elements.security.ProfileSupplierProvider;
import dev.getelements.elements.security.UserAuthenticationMethod;
import dev.getelements.elements.security.UserProvider;

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
