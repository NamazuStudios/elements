package dev.getelements.elements.git;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.*;
import dev.getelements.elements.servlet.security.HttpRequestAttributeAuthenticationMethod;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Created by patricktwohig on 8/3/17.
 */
public class GitSecurityModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(User.class).toProvider(UserProvider.class).in(ServletScopes.REQUEST);
        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);
        bind(new TypeLiteral<Optional<Profile>>(){}).toProvider(ProfileOptionalSupplier.class);

        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpRequestAttributeAuthenticationMethod.class);

        final Multibinder<ProfileIdentificationMethod> profileIdentificationMethodMultibinder;
        profileIdentificationMethodMultibinder = Multibinder.newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().toInstance(ProfileIdentificationMethod.UNIDENTIFIED);

    }
}
