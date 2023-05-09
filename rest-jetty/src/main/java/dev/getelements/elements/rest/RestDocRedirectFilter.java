package dev.getelements.elements.rest;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

import static dev.getelements.elements.Constants.API_OUTSIDE_URL;
import static dev.getelements.elements.Constants.DOC_OUTSIDE_URL;
import static dev.getelements.elements.util.URIs.appendOrReplaceQuery;
import static dev.getelements.elements.util.URIs.appendPath;
import static java.lang.String.format;

public class RestDocRedirectFilter implements Filter {

    public static final String DOC_URL = "url";

    private URI apiOutsideUrl;

    private URI docOutsideUrl;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest request,
                         final ServletResponse response,
                         final FilterChain chain) throws IOException, ServletException {

        final var httpServletResponse = (HttpServletResponse) response;

        final var restApiSwaggerJsonUrl = appendPath(getApiOutsideUrl(), "swagger.json");

        final var swaggerUiUrl = appendPath(getDocOutsideUrl(), "swagger");
        final var query = format("%s=%s", DOC_URL, restApiSwaggerJsonUrl.toString());

        final var location = appendOrReplaceQuery(swaggerUiUrl, query);
        httpServletResponse.setHeader("Location", location.toString());
        httpServletResponse.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);

    }

    @Override
    public void destroy() {}

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
