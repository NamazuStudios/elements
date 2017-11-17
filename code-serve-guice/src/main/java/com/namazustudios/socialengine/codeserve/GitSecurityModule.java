package com.namazustudios.socialengine.codeserve;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;
import com.namazustudios.socialengine.security.UserProvider;
import com.namazustudios.socialengine.security.HttpRequestAttributeAuthenticationMethod;

/**
 * Created by patricktwohig on 8/3/17.
 */
public class GitSecurityModule extends AbstractModule {
    @Override
    protected void configure() {

        bind(User.class).toProvider(UserProvider.class).in(ServletScopes.REQUEST);

        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpRequestAttributeAuthenticationMethod.class);

    }
}
