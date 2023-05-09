package dev.getelements.elements.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.security.*;
import dev.getelements.elements.servlet.security.*;

import java.util.function.Supplier;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class StandardServletSecurityModule extends AbstractModule {

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
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestHeaderProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestAttributeProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(HttpRequestSessionSecretProfileIdentificationMethod.class);

    }

}
