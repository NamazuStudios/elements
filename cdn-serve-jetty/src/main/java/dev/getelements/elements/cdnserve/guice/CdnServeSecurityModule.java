package dev.getelements.elements.cdnserve.guice;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.cdnserve.api.DeploymentService;
import dev.getelements.elements.cdnserve.api.DeploymentServiceProvider;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.rt.guice.RequestScope;
import dev.getelements.elements.security.UserAuthenticationMethod;
import dev.getelements.elements.security.UserProvider;
import dev.getelements.elements.servlet.security.HttpRequestAttributeAuthenticationMethod;
import dev.getelements.elements.servlet.security.HttpSessionUserAuthenticationMethod;

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
