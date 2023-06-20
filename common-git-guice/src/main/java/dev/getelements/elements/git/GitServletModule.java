package dev.getelements.elements.git;

import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.google.inject.servlet.ServletScopes;
import dev.getelements.elements.git.GitServletProvider;
import dev.getelements.elements.git.HttpServletRepositoryResolver;
import dev.getelements.elements.servlet.security.HttpServletBasicAuthFilter;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class GitServletModule extends ServletModule {

    @Override
    protected void configureServlets() {

        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
        bind(GitServlet.class).toProvider(GitServletProvider.class).asEagerSingleton();

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){})
                .to(HttpServletRepositoryResolver.class);

        // Serving Configuration.
        serve("/git/*").with(GitServlet.class);

        // The basic auth filter and the global secret filter.
        filter("/git/*").through(HttpServletBasicAuthFilter.class);

    }

}
