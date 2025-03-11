package dev.getelements.elements.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;
import dev.getelements.elements.sdk.model.security.UserAuthenticationMethod;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.security.ProfileOptionalSupplier;
import dev.getelements.elements.security.ProfileSupplierProvider;
import dev.getelements.elements.security.UserProvider;
import dev.getelements.elements.servlet.security.HttpRequestAttributeAuthenticationMethod;
import dev.getelements.elements.servlet.security.HttpRequestAttributeProfileIdentificationMethod;
import dev.getelements.elements.servlet.security.HttpRequestHeaderProfileIdentificationMethod;
import dev.getelements.elements.servlet.security.HttpRequestSessionSecretProfileIdentificationMethod;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class StandardServletSecurityModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(User.class).toProvider(UserProvider.class);

        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);
        bind(new TypeLiteral<Optional<Profile>>(){}).toProvider(ProfileOptionalSupplier.class);

        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpRequestAttributeAuthenticationMethod.class);

        final Multibinder<ProfileIdentificationMethod>profileIdentificationMethodMultibinder;
        profileIdentificationMethodMultibinder = Multibinder.newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestHeaderProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestAttributeProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestSessionSecretProfileIdentificationMethod.class);

    }

}
