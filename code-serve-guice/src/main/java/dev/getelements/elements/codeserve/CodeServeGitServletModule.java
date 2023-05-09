package dev.getelements.elements.codeserve;

import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import dev.getelements.elements.servlet.security.HttpServletBasicAuthFilter;
import dev.getelements.elements.servlet.security.HttpServletGlobalSecretHeaderFilter;
import dev.getelements.elements.servlet.security.VersionServlet;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class CodeServeGitServletModule extends ServletModule {
    @Override
    protected void configureServlets() {

        bind(VersionServlet.class).asEagerSingleton();
        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
        bind(GitServlet.class).toProvider(GitServletProvider.class).asEagerSingleton();

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){}).to(CodeServeRepositoryResolver.class);

        // Serving Configuration.
        serve("/version").with(VersionServlet.class);
        serve("/git/*").with(GitServlet.class);

        // The basic auth filter and the global secret filter.
        filter("/git/*").through(HttpServletBasicAuthFilter.class);

    }
}
