package com.namazustudios.socialengine.rest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.rest.security.RequestAttributeAuthenticationMethod;
import com.namazustudios.socialengine.rest.security.HttpSessionUserAuthenticationMethod;
import com.namazustudios.socialengine.rest.security.UserAuthenticationMethod;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class SecurityModule extends AbstractModule {

    @Override
    protected void configure() {
        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(RequestAttributeAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpSessionUserAuthenticationMethod.class);
    }

}
