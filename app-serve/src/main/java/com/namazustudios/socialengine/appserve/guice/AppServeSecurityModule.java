package com.namazustudios.socialengine.appserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.appserve.RequestAttributeProfileIdentificationMethod;
import com.namazustudios.socialengine.appserve.RequestHeaderProfileIdentificationMethod;
import com.namazustudios.socialengine.appserve.RequestSessionSecretProfileIdentificationMethod;
import com.namazustudios.socialengine.appserve.RequestUserAuthenticationMethod;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.security.ProfileIdentificationMethod;
import com.namazustudios.socialengine.security.ProfileSupplierProvider;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;
import com.namazustudios.socialengine.security.UserProvider;

import java.util.function.Supplier;

public class AppServeSecurityModule extends PrivateModule {

    @Override
    protected void configure() {

        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(RequestUserAuthenticationMethod.class);

        final Multibinder<ProfileIdentificationMethod>profileIdentificationMethodMultibinder;
        profileIdentificationMethodMultibinder = Multibinder.newSetBinder(binder(), ProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(RequestHeaderProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(RequestSessionSecretProfileIdentificationMethod.class);
        profileIdentificationMethodMultibinder.addBinding().to(RequestAttributeProfileIdentificationMethod.class);

        bind(User.class).toProvider(UserProvider.class).in(RequestScope.getInstance());
        bind(new TypeLiteral<Supplier<Profile>>(){}).toProvider(ProfileSupplierProvider.class);

        expose(User.class);
        expose(new TypeLiteral<Supplier<Profile>>(){});

    }

}
