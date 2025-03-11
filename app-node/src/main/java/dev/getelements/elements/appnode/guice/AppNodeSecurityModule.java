package dev.getelements.elements.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.appnode.security.ResourceOptionalSessionProvider;
import dev.getelements.elements.appnode.security.ResourceProfileIdentificationMethod;
import dev.getelements.elements.sdk.model.security.ProfileIdentificationMethod;
import dev.getelements.elements.sdk.model.security.UserAuthenticationMethod;
import dev.getelements.elements.security.ProfileOptionalSupplier;
import dev.getelements.elements.security.ProfileSupplierProvider;
import dev.getelements.elements.security.SessionProvider;
import dev.getelements.elements.appnode.security.ResourceUserAuthenticationMethod;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.rt.security.SessionProfileIdentificationMethod;
import dev.getelements.elements.rt.security.SessionUserAuthenticationMethod;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

public class AppNodeSecurityModule extends PrivateModule {

    @Override
    protected void configure() {

        final var userAuthenticationMethodMultibinder = newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(SessionUserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(ResourceUserAuthenticationMethod.class);

        final var profileIdentificationMethodMultibinder = newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(SessionProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(ResourceProfileIdentificationMethod.class);

        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);
        bind(new TypeLiteral<Optional<Profile>>(){}).toProvider(ProfileOptionalSupplier.class);

        var optionalSessionKey = Key.get(new TypeLiteral<Optional<Session>>(){});

        bind(Session.class).toProvider(SessionProvider.class);
        bind(optionalSessionKey).toProvider(ResourceOptionalSessionProvider.class);

        expose(optionalSessionKey);

        expose(User.class);
        expose(Session.class);
        expose(optionalSessionKey);
        expose(new TypeLiteral<Supplier<Profile>>(){});
        expose(new TypeLiteral<Optional<Profile>>(){});

    }

}
