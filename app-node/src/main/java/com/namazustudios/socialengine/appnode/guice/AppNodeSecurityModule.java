package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.appnode.security.ResourceOptionalSessionProvider;
import com.namazustudios.socialengine.appnode.security.ResourceProfileIdentificationMethod;
import com.namazustudios.socialengine.security.SessionProvider;
import com.namazustudios.socialengine.appnode.security.ResourceUserAuthenticationMethod;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.session.Session;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.rt.security.SessionProfileIdentificationMethod;
import com.namazustudios.socialengine.rt.security.SessionUserAuthenticationMethod;
import com.namazustudios.socialengine.security.*;

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


        var optionalSessionKey = Key.get(new TypeLiteral<Optional<Session>>(){});

        bind(Session.class).toProvider(SessionProvider.class);
        bind(optionalSessionKey).toProvider(ResourceOptionalSessionProvider.class);

        expose(optionalSessionKey);

        expose(User.class);
        expose(Session.class);
        expose(optionalSessionKey);
        expose(new TypeLiteral<Supplier<Profile>>(){});

    }

}
