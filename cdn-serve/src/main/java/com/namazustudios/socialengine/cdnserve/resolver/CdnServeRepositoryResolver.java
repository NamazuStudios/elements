package com.namazustudios.socialengine.cdnserve.resolver;

import com.namazustudios.socialengine.codeserve.ApplicationRepositoryResolver;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.rt.git.GitLoader;
import com.namazustudios.socialengine.service.ApplicationService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.namazustudios.socialengine.model.user.User.Level.SUPERUSER;

/**
 * Created by garrettmcspadden on 12/21/20.
 */
public class CdnServeRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private static final Logger logger = LoggerFactory.getLogger(CdnServeRepositoryResolver.class);

    private GitLoader gitLoader;

    private Provider<User> userProvider;

    private ApplicationService applicationService;

    private ApplicationRepositoryResolver applicationRepositoryResolver;

    @Override
    public Repository open(HttpServletRequest req, String name) throws
            RepositoryNotFoundException,
            ServiceNotAuthorizedException,
            ServiceNotEnabledException,
            ServiceMayNotContinueException {

        final User user = getUserProvider().get();

        if (SUPERUSER.equals(user.getLevel())) {
            return getRepositoryForApplication(user, name);
        }

        throw new RepositoryNotFoundException(name);

    }

    private Repository getRepositoryForApplication(final User user, final String name) throws
            RepositoryNotFoundException,
            ServiceNotAuthorizedException,
            ServiceNotEnabledException,
            ServiceMayNotContinueException{

        final Application application;

        try {
            application = getApplicationService().getApplication(name);
        } catch (NotFoundException ex) {
            throw new RepositoryNotFoundException(name);
        }

        try {
            logger.info("Resolving content repository for application {}", application.getId());
            return getApplicationRepositoryResolver().resolve(application, r -> {
                getGitLoader().performInGit(application.getId(), (g, f) -> doInit(user, g));
                logger.info("Created content repository for application {} ({})", application.getName(), application.getId());
            });
        } catch (RepositoryNotFoundException   |
                 ServiceNotAuthorizedException |
                 ServiceNotEnabledException    |
                 ServiceMayNotContinueException ex) {
            throw ex;
        } catch (Exception e) {
            logger.error("Caught unexpected exception.", e);
            throw new ServiceMayNotContinueException(e);
        }

    }

    private void doInit(final User user, final Git git) {
        checkMainBranch(git);
        commit(user, git);
        push(git);
    }

    private void checkMainBranch(final Git git) {
        try {

            final String branch = git.getRepository().getBranch();

            if (GitLoader.DEFAULT_MAIN_BRANCH.equals(branch)) {
                logger.info("Using git branch {}", branch);
            } else {
                throw new InternalException("Invalid branch checked out: " + branch);
            }

        } catch (IOException ex) {
            throw new InternalException(ex);
        }
    }

    private void commit(final User user, final Git git) {
        try {
            git.commit()
                    .setMessage("Initial Commit")
                    .setCommitter(user.getName(), user.getEmail())
                    .call();
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    private void push(final Git git) {
        try {
            git.push().call();
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    public Provider<User> getUserProvider() {
        return userProvider;
    }

    @Inject
    public void setUserProvider(Provider<User> userProvider) {
        this.userProvider = userProvider;
    }

    public ApplicationService getApplicationService() {
        return applicationService;
    }

    @Inject
    public void setApplicationService(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public ApplicationRepositoryResolver getApplicationRepositoryResolver() {
        return applicationRepositoryResolver;
    }

    @Inject
    public void setApplicationRepositoryResolver(ApplicationRepositoryResolver applicationRepositoryResolver) {
        this.applicationRepositoryResolver = applicationRepositoryResolver;
    }

    public GitLoader getGitLoader() {
        return gitLoader;
    }

    @Inject
    public void setGitLoader(GitLoader gitLoader) {
        this.gitLoader = gitLoader;
    }

}
