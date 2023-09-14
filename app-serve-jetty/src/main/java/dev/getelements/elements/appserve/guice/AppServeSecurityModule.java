package dev.getelements.elements.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.appserve.RequestHeaderProfileIdentificationMethod;
import dev.getelements.elements.appserve.SessionSecretHeaderProfileIdentificationMethod;
import dev.getelements.elements.rt.security.SessionUserAuthenticationMethod;
import dev.getelements.elements.appserve.provider.RequestOptionalSessionProvider;
import dev.getelements.elements.appserve.provider.RequestSessionSecretHeaderProvider;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.session.Session;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.guice.RequestScope;
import dev.getelements.elements.security.*;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

@Deprecated
public class AppServeSecurityModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(SessionSecretHeader.class).toProvider(RequestSessionSecretHeaderProvider.class);
        bind(new TypeLiteral<Optional<Session>>(){}).toProvider(RequestOptionalSessionProvider.class);

        final var userAuthenticationMethodMultibinder = newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(SessionUserAuthenticationMethod.class);

        final var profileIdentificationMethodMultibinder = newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(RequestHeaderProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(SessionSecretHeaderProfileIdentificationMethod.class);

        bind(User.class).toProvider(UserProvider.class).in(RequestScope.getInstance());
        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);
        bind(new TypeLiteral<Optional<Profile>>(){}).toProvider(ProfileOptionalSupplier.class);

        expose(User.class);
        expose(SessionSecretHeader.class);
        expose(new TypeLiteral<Optional<Session>>(){});
        expose(new TypeLiteral<Supplier<Profile>>(){});
        expose(new TypeLiteral<Optional<Profile>>(){});

    }

}
