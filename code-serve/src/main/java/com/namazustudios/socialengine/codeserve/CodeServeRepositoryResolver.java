package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.dao.BootstrapDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;
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

import static com.namazustudios.socialengine.model.User.Level.SUPERUSER;

/**
 * Created by patricktwohig on 8/1/17.
 */
public class CodeServeRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private static final Logger logger = LoggerFactory.getLogger(CodeServeRepositoryResolver.class);

    private Provider<User> userProvider;

    private BootstrapDao bootstrapDao;

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
            logger.info("Resolving repository for application {}", application.getId());
            return getApplicationRepositoryResolver().resolve(application, r -> {

                logger.info("Created repository for application {} ({})", application.getName(), application.getId());

                logger.info("Bootstrapping application repository {} ({})", application.getName(), application.getId());
                getBootstrapDao().bootstrap(user, application);

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

    public BootstrapDao getBootstrapDao() {
        return bootstrapDao;
    }

    @Inject
    public void setBootstrapDao(BootstrapDao bootstrapDao) {
        this.bootstrapDao = bootstrapDao;
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
