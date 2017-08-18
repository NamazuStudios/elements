package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link ApplicationService} implemented for when the current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    private URI codeServeUrl;

    private ApplicationDao applicationDao;

    @Override
    public Pagination<Application> getApplications() {
        return getApplicationDao().getActiveApplications();
    }

    @Override
    public Application createApplication(Application application) {
        return addCodeServeUrl(getApplicationDao().createOrUpdateInactiveApplication(application));
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        return getApplicationDao().getActiveApplications(offset, count).transform(this::addCodeServeUrl);
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        return getApplicationDao().getActiveApplications(offset, count, search).transform(this::addCodeServeUrl);
    }

    @Override
    public Application getApplication(String nameOrId) {
        return addCodeServeUrl(getApplicationDao().getActiveApplication(nameOrId));
    }

    @Override
    public Application updateApplication(String nameOrId, Application application) {
        return addCodeServeUrl(getApplicationDao().updateActiveApplication(nameOrId, application));
    }

    @Override
    public void deleteApplication(String nameOrId) {
        getApplicationDao().softDeleteApplication(nameOrId);
    }

    private Application addCodeServeUrl(final Application application) {
        final URI base = determineBaseUrl();
        final URI repositoryRoot = base.resolve(application.getName());
        application.setScriptRepoUrl(repositoryRoot.toString());
        return application;
    }

    private URI determineBaseUrl() {

        final URI codeServeUrl = getCodeServeUrl();

        if (getCodeServeUrl().getPath().endsWith("/")) {
            return codeServeUrl;
        } else {
            try {
                return new URI(codeServeUrl.getScheme(),
                               codeServeUrl.getUserInfo(),
                               codeServeUrl.getHost(),
                               codeServeUrl.getPort(),
                               codeServeUrl.getPath()  + "/",
                               codeServeUrl.getQuery(),
                               codeServeUrl.getFragment());
            } catch (URISyntaxException e) {
                throw new InternalException(e);
            }
        }

    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

    public URI getCodeServeUrl() {
        return codeServeUrl;
    }

    @Inject
    public void setCodeServeUrl(@Named(Constants.CODE_SERVE_URL) URI codeServeUrl) {
        this.codeServeUrl = codeServeUrl;
    }

}
