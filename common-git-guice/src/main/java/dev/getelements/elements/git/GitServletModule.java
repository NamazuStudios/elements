package dev.getelements.elements.git;

import com.google.inject.TypeLiteral;
import dev.getelements.elements.guice.BaseServletModule;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Created by patricktwohig on 8/2/17.
 */
@Deprecated
public class GitServletModule extends BaseServletModule {

    @Override
    protected void configureServlets() {

        bind(GitServlet.class).toProvider(GitServletProvider.class).asEagerSingleton();

        serve("/git/*").with(GitServlet.class);

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){})
                .to(HttpServletRepositoryResolver.class);

        useHttpBasicSecurityFilters();

    }

}
