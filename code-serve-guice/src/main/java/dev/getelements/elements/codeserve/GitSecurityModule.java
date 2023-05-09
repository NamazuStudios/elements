package dev.getelements.elements.codeserve;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.security.UserAuthenticationMethod;
import dev.getelements.elements.security.UserProvider;
import dev.getelements.elements.servlet.security.HttpRequestAttributeAuthenticationMethod;

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
