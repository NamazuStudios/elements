package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

import static com.namazustudios.socialengine.util.URIs.appendPath;

/**
 * {@link ApplicationService} implemented for when the current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    private URI codeServeUrl;

    private URI httpTunnelUrl;

    private URI apiOutsiudeUrl;

    private ApplicationDao applicationDao;

    @Override
    public Pagination<Application> getApplications() {
        return getApplicationDao().getActiveApplications();
    }

    @Override
    public Application createApplication(Application application) {
        return addPrivilegedUrls(getApplicationDao().createOrUpdateInactiveApplication(application));
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count) {
        return getApplicationDao().getActiveApplications(offset, count).transform(this::addPrivilegedUrls);
    }

    @Override
    public Pagination<Application> getApplications(int offset, int count, String search) {
        return getApplicationDao().getActiveApplications(offset, count, search).transform(this::addPrivilegedUrls);
    }

    @Override
    public Application getApplication(String nameOrId) {
        return addPrivilegedUrls(getApplicationDao().getActiveApplication(nameOrId));
    }

    @Override
    public Application updateApplication(String nameOrId, Application application) {
        return addPrivilegedUrls(getApplicationDao().updateActiveApplication(nameOrId, application));
    }

    @Override
    public void deleteApplication(String nameOrId) {
        getApplicationDao().softDeleteApplication(nameOrId);
    }

    private Application addPrivilegedUrls(final Application application) {
        addCodeServeUrl(application);
        addHttpTunnelUrl(application);
        addDocumentationUrl(application);
        return application;
    }

    private void addCodeServeUrl(final Application application) {
        final URI base = appendPath(getCodeServeUrl(), application.getName());
        final URI repositoryRoot = base.resolve(application.getName());
        application.setScriptRepoUrl(repositoryRoot.toString());
    }

    private void addHttpTunnelUrl(final Application application) {
        final URI base = appendPath(getHttpTunnelUrl(), application.getName());
        final URI httpTunnelEndpointUrl = base.resolve(application.getName());
        application.setHttpTunnelEndpointUrl(httpTunnelEndpointUrl.toString());
    }

    private void addDocumentationUrl(final Application application) {
        final URI documentationUrl = appendPath(getApiOutsiudeUrl(), "application", application.getId(), "swagger.json");
        application.setHttpDocumentationUrl(documentationUrl.toString());
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

    public URI getHttpTunnelUrl() {
        return httpTunnelUrl;
    }

    @Inject
    public void setHttpTunnelUrl(@Named(Constants.HTTP_TUNNEL_URL) URI httpTunnelUrl) {
        this.httpTunnelUrl = httpTunnelUrl;
    }

    public URI getApiOutsiudeUrl() {
        return apiOutsiudeUrl;
    }

    @Inject
    public void setApiOutsiudeUrl(@Named(Constants.API_OUTSIDE_URL) URI apiOutsiudeUrl) {
        this.apiOutsiudeUrl = apiOutsiudeUrl;
    }

}
