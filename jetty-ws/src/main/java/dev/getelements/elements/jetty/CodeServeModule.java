package dev.getelements.elements.jetty;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.codeserve.guice.CodeServeStorageModule;
import dev.getelements.elements.git.GitServletProvider;
import dev.getelements.elements.git.HttpServletRepositoryResolver;
import dev.getelements.elements.guice.ServletBindings;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.security.UserProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import static com.google.inject.name.Names.named;

public class CodeServeModule extends PrivateModule {

    public static final String GIT_ROOT = "/code/*";

    public static final String GIT_SERVLET_NAME = "dev.getelements.elements.code.git.servlet";

    public static final Key<GitServlet> GIT_SERVLET_KEY = Key.get(GitServlet.class, named(GIT_SERVLET_NAME));

    @Override
    protected void configure() {

        install(new CodeServeStorageModule());

//        bind(User.class).toProvider(UserProvider.class);

        bind(GIT_SERVLET_KEY)
                .toProvider(GitServletProvider.class)
                .asEagerSingleton();

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){})
                .to(HttpServletRepositoryResolver.class);

        expose(GIT_SERVLET_KEY);

    }

    public void accept(final ServletBindings bindings) {
        bindings.useBasicAuthFor(GIT_ROOT);
        bindings.serve(GIT_ROOT).with(GIT_SERVLET_KEY);
    }

}
