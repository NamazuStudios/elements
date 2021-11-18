package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.model.application.Application;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.util.URIs.appendOrReplaceQuery;
import static com.namazustudios.socialengine.util.URIs.appendPath;
import static java.lang.String.format;

public class ApplicationUrls {

    public static final String DOC_URL = "url";

    public static final String GIT_PREFIX = "git";

    private static final String API_SUFFIX = "rest";

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
        final var base = appendPath(getCodeServeUrl(), GIT_PREFIX, application.getName());
        final var repositoryRoot = base.resolve(application.getName());
        application.setScriptRepoUrl(repositoryRoot.toString());
    }

    public void addHttpTunnelUrl(final Application application) {
        final var base = appendPath(getHttpTunnelUrl(), application.getName());
        final var httpTunnelEndpointUrl = base.resolve(format("%s/%s", application.getName(), API_SUFFIX));
        application.setHttpTunnelEndpointUrl(httpTunnelEndpointUrl.toString());
    }

    public void addDocumentationUrl(final Application application) {

        final var httpDocumentationUrl = appendPath(
            getDocOutsideUrl(),
            "rest",
            "swagger",
            "2",
            application.getName(),
            "swagger.json"
        );

        application.setHttpDocumentationUrl(httpDocumentationUrl.toString());
        addDocumentationUiUrl(httpDocumentationUrl, application);

    }

    private void addDocumentationUiUrl(final URI httpDocumentationUrl, final Application application) {
        final var swaggerUiUrl = appendPath(getDocOutsideUrl(), "swagger");
        final var query = format("%s=%s", DOC_URL, httpDocumentationUrl.toString());
        final URI documentationUiUri = appendOrReplaceQuery(swaggerUiUrl, query);
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
