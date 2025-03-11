package dev.getelements.elements.git;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.application.ApplicationService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.sdk.model.user.User.Level.SUPERUSER;
import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Created by patricktwohig on 8/1/17.
 */
public class HttpServletRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRepositoryResolver.class);

    private Provider<User> userProvider;

    private ApplicationService applicationService;

    private ApplicationRepositoryResolver applicationRepositoryResolver;

    @Override
    public Repository open(final HttpServletRequest req, final String name) throws
            RepositoryNotFoundException,
            ServiceNotAuthorizedException,
            ServiceNotEnabledException,
            ServiceMayNotContinueException {

        final User user = getUserProvider().get();

        if (SUPERUSER.equals(user.getLevel())) {
            return getRepositoryForApplication(name);
        }

        throw new RepositoryNotFoundException(name);

    }

    private Repository getRepositoryForApplication(final String name) throws
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

            logger.info("Resolving repository for application {}", application.getId());

            return getApplicationRepositoryResolver().resolve(
                    application,
                    r -> logger.info("Created repository for application {} ({})",
                            application.getName(),
                            application.getId())
            );

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
    public void setApplicationService(@Named(UNSCOPED) ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public ApplicationRepositoryResolver getApplicationRepositoryResolver() {
        return applicationRepositoryResolver;
    }

    @Inject
    public void setApplicationRepositoryResolver(ApplicationRepositoryResolver applicationRepositoryResolver) {
        this.applicationRepositoryResolver = applicationRepositoryResolver;
    }

}
