package com.namazustudios.socialengine.codeserve;

import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by patricktwohig on 8/2/17.
 */
public class GitServletProvider implements Provider<GitServlet> {

    private Provider<RepositoryResolver<HttpServletRequest>> servletRequestRepositoryResolverProvider;

    @Override
    public GitServlet get() {
        final GitServlet gitServlet = new GitServlet();

        final RepositoryResolver<HttpServletRequest> servletRequestRepositoryResolver;
        servletRequestRepositoryResolver = getServletRequestRepositoryResolverProvider().get();
        gitServlet.setRepositoryResolver(servletRequestRepositoryResolver);

        return gitServlet;
    }

    public Provider<RepositoryResolver<HttpServletRequest>> getServletRequestRepositoryResolverProvider() {
        return servletRequestRepositoryResolverProvider;
    }

    @Inject
    public void setServletRequestRepositoryResolverProvider(Provider<RepositoryResolver<HttpServletRequest>> servletRequestRepositoryResolverProvider) {
        this.servletRequestRepositoryResolverProvider = servletRequestRepositoryResolverProvider;
    }

}

