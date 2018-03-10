package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.application.Application;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

import static com.google.common.net.UrlEscapers.urlFragmentEscaper;
import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.util.URIs.appendOrReplaceQuery;
import static com.namazustudios.socialengine.util.URIs.appendPath;
import static java.lang.String.format;

public class ApplicationUrls {

    public static final String GIT_PREFIX = "git";

    private static final String CONFIG_PARAM = "url";

    private URI codeServeUrl;

    private URI httpTunnelUrl;

    private URI apiOutsideUrl;

    private URI docOutsideUrl;

    public Application addAllUrls(final Application application) {
        addPublicUrls(application);
        addCodeServeUrl(application);
        return application;
    }

    public Application addPublicUrls(final Application application) {
        addHttpTunnelUrl(application);
        addDocumentationUrl(application);
        return application;
    }

    public void addCodeServeUrl(final Application application) {
        final URI base = appendPath(getCodeServeUrl(), GIT_PREFIX, application.getName());
        final URI repositoryRoot = base.resolve(application.getName());
        application.setScriptRepoUrl(repositoryRoot.toString());
    }

    public void addHttpTunnelUrl(final Application application) {
        final URI base = appendPath(getHttpTunnelUrl(), application.getName());
        final URI httpTunnelEndpointUrl = base.resolve(application.getName());
        application.setHttpTunnelEndpointUrl(httpTunnelEndpointUrl.toString());
    }

    public void addDocumentationUrl(final Application application) {
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
