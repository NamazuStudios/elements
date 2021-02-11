package com.namazustudios.socialengine.cdnserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletScopes;
import com.namazustudios.socialengine.cdnserve.api.DeploymentService;
import com.namazustudios.socialengine.cdnserve.api.DeploymentServiceProvider;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.guice.RequestScope;
import com.namazustudios.socialengine.security.UserAuthenticationMethod;
import com.namazustudios.socialengine.security.UserProvider;
import com.namazustudios.socialengine.servlet.security.HttpRequestAttributeAuthenticationMethod;
import com.namazustudios.socialengine.servlet.security.HttpSessionUserAuthenticationMethod;

public class CdnServeSecurityModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(User.class).toProvider(UserProvider.class).in(ServletScopes.REQUEST);
        final Multibinder<UserAuthenticationMethod> userAuthenticationMethodMultibinder;
        userAuthenticationMethodMultibinder = Multibinder.newSetBinder(binder(), UserAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpRequestAttributeAuthenticationMethod.class);
        userAuthenticationMethodMultibinder.addBinding().to(HttpSessionUserAuthenticationMethod.class);

        bind(DeploymentService.class).toProvider(DeploymentServiceProvider.class).in(ServletScopes.REQUEST);

        expose(User.class);
        expose(DeploymentService.class);
    }
}
