package dev.getelements.elements.docserve;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import static dev.getelements.elements.Constants.*;
import static dev.getelements.elements.util.URIs.appendPath;
import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static javax.servlet.http.HttpServletResponse.SC_MOVED_PERMANENTLY;

@Singleton
public class DocRedirectFilter implements Filter {

    private URI apiOutsideUrl;

    private URI docOutsideUrl;

    private String httpPathPrefix;

    private String swaggerIndexHtml;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(final ServletRequest _request,
                         final ServletResponse _response,
                         final FilterChain chain) throws IOException, ServletException {

        final var request = (HttpServletRequest) _request;
        final var response = (HttpServletResponse) _response;
        final var requestURI = request.getRequestURI().replaceAll("/$", "");

        if (requestURI.equals(getSwaggerIndexHtml()) && request.getParameter("url") == null) {
            redirect(response);
        }

        chain.doFilter(request, response);

    }

    private void redirect(final HttpServletResponse response) throws UnsupportedEncodingException {

        final var restApiSwaggerJson = appendPath(getApiOutsideUrl(), "swagger.json");

        final var docSwaggerPage = appendPath(getDocOutsideUrl(), "swagger");

        final URI location;

        try {
            location = new URI(
                docSwaggerPage.getScheme(),
                docSwaggerPage.getUserInfo(),
                docSwaggerPage.getHost(),
                docSwaggerPage.getPort(),
                docSwaggerPage.getPath(),
                format("url=%s", restApiSwaggerJson.toString()),
                null
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        response.setStatus(SC_MOVED_PERMANENTLY);
        response.setHeader("Location", location.toString());

    }

    @Override
    public void destroy() {

    }

    public URI getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(API_OUTSIDE_URL) final URI apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

    public URI getDocOutsideUrl() {
        return docOutsideUrl;
    }

    @Inject
    public void setDocOutsideUrl(@Named(DOC_OUTSIDE_URL) final URI docOutsideUrl) {
        this.docOutsideUrl = docOutsideUrl;
    }

    public String getHttpPathPrefix() {
        return httpPathPrefix;
    }

    public String getSwaggerIndexHtml() {
        return swaggerIndexHtml;
    }

    @Inject
    public void setHttpPathPrefix(@Named(HTTP_PATH_PREFIX) final String httpPathPrefix) {
        this.httpPathPrefix = httpPathPrefix;
        this.swaggerIndexHtml = format("%s/swagger/index.html", getHttpPathPrefix());
        this.swaggerIndexHtml = swaggerIndexHtml.startsWith("/") ?
            this.swaggerIndexHtml :
            "/" + this.swaggerIndexHtml;
    }

}

