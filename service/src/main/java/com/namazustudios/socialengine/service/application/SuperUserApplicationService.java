package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.service.ApplicationService;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

import static com.google.common.net.UrlEscapers.urlFragmentEscaper;
import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.util.URIs.appendOrReplaceQuery;
import static com.namazustudios.socialengine.util.URIs.appendPath;
import static java.lang.String.format;

/**
 * {@link ApplicationService} implemented for when the current user has {@link User.Level#SUPERUSER} access.
 *
 * Created by patricktwohig on 7/10/15.
 */
public class SuperUserApplicationService implements ApplicationService {

    private static final String GIT_PREFIX = "git";

    private static final String CONFIG_PARAM = "config";

    private URI codeServeUrl;

    private URI httpTunnelUrl;

    private URI apiOutsideUrl;

    private URI docOutsideUrl;

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
        final URI base = appendPath(getCodeServeUrl(), GIT_PREFIX, application.getName());
        final URI repositoryRoot = base.resolve(application.getName());
        application.setScriptRepoUrl(repositoryRoot.toString());
    }

    private void addHttpTunnelUrl(final Application application) {
        final URI base = appendPath(getHttpTunnelUrl(), application.getName());
        final URI httpTunnelEndpointUrl = base.resolve(application.getName());
        application.setHttpTunnelEndpointUrl(httpTunnelEndpointUrl.toString());
    }

    private void addDocumentationUrl(final Application application) {
        final URI httpDocumentationUrl = appendPath(getApiOutsideUrl(), "application", application.getId(), "swagger.json");
        application.setHttpDocumentationUrl(httpDocumentationUrl.toString());
        addDocumentationUiUrl(httpDocumentationUrl, application);
    }

    private void addDocumentationUiUrl(final URI httpDocumentationUrl, final Application application) {
        final String encoded = urlFragmentEscaper().escape(httpDocumentationUrl.toString());
        final String fragment = format("%s=%s", CONFIG_PARAM, encoded);
        final URI documentationUiUri = appendOrReplaceQuery(getDocOutsideUrl(), fragment);
        application.setHttpDocumentationUiUrl(documentationUiUri.toString());
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
    public void setCodeServeUrl(@Named(CODE_SERVE_URL) URI codeServeUrl) {
        this.codeServeUrl = codeServeUrl;
    }

    public URI getHttpTunnelUrl() {
        return httpTunnelUrl;
    }

    @Inject
    public void setHttpTunnelUrl(@Named(HTTP_TUNNEL_URL) URI httpTunnelUrl) {
        this.httpTunnelUrl = httpTunnelUrl;
    }

    public URI getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(API_OUTSIDE_URL) URI apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

    public URI getDocOutsideUrl() {
        return docOutsideUrl;
    }

    @Inject
    public void setDocOutsideUrl(@Named(DOC_OUTSIDE_URL) URI docOutsideUrl) {
        this.docOutsideUrl = docOutsideUrl;
    }

}
