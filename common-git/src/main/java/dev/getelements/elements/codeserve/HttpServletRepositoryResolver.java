package dev.getelements.elements.codeserve;

import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.ApplicationBootstrapper;
import dev.getelements.elements.rt.id.ApplicationId;
import dev.getelements.elements.service.ApplicationService;
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

import static dev.getelements.elements.model.user.User.Level.SUPERUSER;

/**
 * Created by patricktwohig on 8/1/17.
 */
public class HttpServletRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private static final Logger logger = LoggerFactory.getLogger(HttpServletRepositoryResolver.class);

    private Provider<User> userProvider;

    private ApplicationService applicationService;

    private ApplicationBootstrapper applicationBootstrapper;

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

            logger.info("Resolving repository for application {}", application.getId());

            return getApplicationRepositoryResolver().resolve(application, r -> {

                logger.info("Created repository for application {} ({})", application.getName(), application.getId());

                logger.info("Bootstrapping application repository {} ({})", application.getName(), application.getId());

                final var userMetadata = new ApplicationBootstrapper.BootstrapUserMetadata() {

                    @Override
                    public String getName() {
                        return user.getName();
                    }

                    @Override
                    public String getEmail() {
                        return user.getEmail();
                    }

                };

                final var applicationId = ApplicationId.forUniqueName(application.getId());
                getApplicationBootstrapper().bootstrap(userMetadata, applicationId);

                logger.info("Bootstrapped application repository {} ({})", application.getName(), application.getId());

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

    public ApplicationBootstrapper getApplicationBootstrapper() {
        return applicationBootstrapper;
    }

    @Inject
    public void setApplicationBootstrapper(ApplicationBootstrapper applicationBootstrapper) {
        this.applicationBootstrapper = applicationBootstrapper;
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

}
