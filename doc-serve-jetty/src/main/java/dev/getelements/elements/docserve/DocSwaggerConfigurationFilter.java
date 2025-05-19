package dev.getelements.elements.docserve;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import static dev.getelements.elements.sdk.model.Constants.*;
import static dev.getelements.elements.sdk.model.util.URIs.appendPath;
import static java.lang.String.format;

@Singleton
public class DocSwaggerConfigurationFilter implements Filter {

    private URI apiOutsideUrl;

    private static final String SWAGGER_INITIALIZER_JS = "/doc/swagger/swagger-initializer.js";
    private static final String SWAGGER_INITIALIZER_JS_CLASSPATH = "/swagger/swagger-initializer.js";

    private String swaggerInitializerScriptFormat;

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {

        final var builder = new StringBuilder();
        final var charBuffer = CharBuffer.allocate(4096);

        try (var stream = DocSwaggerConfigurationFilter.class.getResourceAsStream(SWAGGER_INITIALIZER_JS_CLASSPATH);
             var reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             var bufferedReader = new BufferedReader(reader)) {
            while (bufferedReader.read(charBuffer) >= 0) {
                builder.append(charBuffer.flip());
                charBuffer.clear();
            }
        } catch (IOException ex) {
            throw new ServletException(ex);
        }

        swaggerInitializerScriptFormat = builder.toString();

    }

    @Override
    public void doFilter(final ServletRequest _request,
                         final ServletResponse _response,
                         final FilterChain chain) throws IOException, ServletException {

        final var request = (HttpServletRequest) _request;
        final var response = (HttpServletResponse) _response;

        if (SWAGGER_INITIALIZER_JS.equals(request.getServletPath())) {
            intercept(response);
        } else {
            chain.doFilter(request, response);
        }

    }

    private void intercept(final HttpServletResponse response) throws IOException, ServletException {

        if (swaggerInitializerScriptFormat == null) {
            throw new IllegalStateException("Not initialized.");
        }

        final var restApiSwaggerJson = appendPath(getApiOutsideUrl(), "openapi.json");
        final var script = format(swaggerInitializerScriptFormat, restApiSwaggerJson);
        response.getWriter().print(script);

    }

    @Override
    public void destroy() {}

    public URI getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(API_OUTSIDE_URL) final URI apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

}