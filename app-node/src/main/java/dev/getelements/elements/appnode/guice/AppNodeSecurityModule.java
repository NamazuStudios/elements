package dev.getelements.elements.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.appnode.security.ResourceOptionalSessionProvider;
import dev.getelements.elements.appnode.security.ResourceProfileIdentificationMethod;
import dev.getelements.elements.security.SessionProvider;
import dev.getelements.elements.appnode.security.ResourceUserAuthenticationMethod;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.guice.RequestScope;
import dev.getelements.elements.rt.security.SessionProfileIdentificationMethod;
import dev.getelements.elements.rt.security.SessionUserAuthenticationMethod;
import dev.getelements.elements.security.*;

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

        bind(Session.class).toProvider(SessionProvider.class);
        bind(User.class).toProvider(UserProvider.class).in(RequestScope.getInstance());

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
