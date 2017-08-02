package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.exception.AuthorizationHeaderParseException;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
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
public class ServletRepositoryResolver implements RepositoryResolver<HttpServletRequest> {

    private AuthService authService;

    private ApplicationService applicationService;

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
            ServiceMayNotContinueException,
            ServiceNotAuthorizedException,
            RepositoryNotFoundException {

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
        return null;
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

}
