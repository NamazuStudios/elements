package com.namazustudios.socialengine.codeserve;

import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.servlet.security.VersionServlet;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class GitServletModule extends ServletModule {
    @Override
    protected void configureServlets() {

        bind(VersionServlet.class).asEagerSingleton();
        bind(BasicAuthFilter.class).asEagerSingleton();
        bind(GitServlet.class).toProvider(GitServletProvider.class).asEagerSingleton();

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){}).to(CodeServeRepositoryResolver.class);

        serve("/version").with(VersionServlet.class);

        serve("/git/*").with(GitServlet.class);
        filter("/git/*").through(BasicAuthFilter.class);

    }
}
