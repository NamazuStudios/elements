package com.namazustudios.socialengine.cdnserve.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.servlet.ServletModule;
import com.namazustudios.socialengine.cdnserve.resolver.CdnServeRepositoryResolver;
import com.namazustudios.socialengine.codeserve.GitServletProvider;
import com.namazustudios.socialengine.servlet.security.HttpServletBasicAuthFilter;
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
        bind(HttpServletBasicAuthFilter.class).asEagerSingleton();
        bind(GitServlet.class).toProvider(GitServletProvider.class).asEagerSingleton();

        bind(new TypeLiteral<RepositoryResolver<HttpServletRequest>>(){}).to(CdnServeRepositoryResolver.class);

    }
}
