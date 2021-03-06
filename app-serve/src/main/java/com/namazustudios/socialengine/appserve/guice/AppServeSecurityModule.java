package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.appserve.RequestHeaderProfileIdentificationMethod;
import com.namazustudios.socialengine.appserve.SessionSecretHeaderProfileIdentificationMethod;
import com.namazustudios.socialengine.rt.security.SessionUserAuthenticationMethod;
import com.namazustudios.socialengine.appserve.provider.RequestOptionalSessionProvider;
import com.namazustudios.socialengine.appserve.provider.RequestSessionSecretHeaderProvider;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.security.*;

import java.util.Optional;
import java.util.function.Supplier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;

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

        expose(User.class);
        expose(SessionSecretHeader.class);
        expose(new TypeLiteral<Optional<Session>>(){});
        expose(new TypeLiteral<Supplier<Profile>>(){});

    }

}
