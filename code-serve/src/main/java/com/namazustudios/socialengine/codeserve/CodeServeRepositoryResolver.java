package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.security.AuthorizationHeader;
import com.namazustudios.socialengine.security.BasicAuthorizationHeader;
import com.namazustudios.socialengine.service.ApplicationService;
import com.namazustudios.socialengine.service.AuthService;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.eclipse.jgit.transport.resolver.RepositoryResolver;
import org.eclipse.jgit.transport.resolver.ServiceNotAuthorizedException;
import org.eclipse.jgit.transport.resolver.ServiceNotEnabledException;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import static com.namazustudios.socialengine.model.User.Level.SUPERUSER;

/**
 * Created by patricktwohig on 8/1/17.
 */
public class CodeServeRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private AuthService authService;

    private ApplicationService applicationService;

    private ApplicationRepositoryResolver applicationRepositoryResolver;

    @Override
    public Repository open(HttpServletRequest req, String name) throws
            RepositoryNotFoundException,
            ServiceNotAuthorizedException,
            ServiceNotEnabledException,
            ServiceMayNotContinueException {
        authorize(req, name);
        return getRepositoryForApplication(name);
    }

    private void authorize(final HttpServletRequest req, final String name) throws
            RepositoryNotFoundException,
            ServiceNotAuthorizedException,
            ServiceNotEnabledException,
            ServiceMayNotContinueException {

        final String header = req.getHeader(AuthorizationHeader.AUTH_HEADER);

        if (header == null) {
            throw new ServiceNotAuthorizedException();
        }

        try {

            final BasicAuthorizationHeader basicAuthHeader;
            basicAuthHeader = new AuthorizationHeader(header).asBasicHeader(req.getCharacterEncoding());

            final User user = getAuthService().loginUser(basicAuthHeader.getUsername(), basicAuthHeader.getPassword());

            if (!SUPERUSER.equals(user.getLevel())) {
                throw new RepositoryNotFoundException(name);
            }

        } catch (AuthorizationHeaderParseException ex) {
            throw new ServiceMayNotContinueException(ex);
        } catch (ForbiddenException ex) {
            throw new ServiceMayNotContinueException(ex);
        }

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
            return getApplicationRepositoryResolver().resolve(application);
        } catch (RepositoryNotFoundException   |
                 ServiceNotAuthorizedException |
                 ServiceNotEnabledException    |
                 ServiceMayNotContinueException ex) {
            throw ex;
        } catch (Exception e) {
            throw new ServiceMayNotContinueException(e);
        }

    }

    public AuthService getAuthService() {
        return authService;
    }

    @Inject
    public void setAuthService(AuthService authService) {
        this.authService = authService;
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
