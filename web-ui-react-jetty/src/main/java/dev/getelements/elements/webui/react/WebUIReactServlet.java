package dev.getelements.elements.webui.react;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.jetty.StaticContentServlet;
import dev.getelements.elements.servlet.HttpContextRoot;
import dev.getelements.elements.servlet.HttpHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;

public class WebUIReactServlet extends StaticContentServlet {

    public static final String RESOURCE_BASE = "static/public";

    private static final String INDEX_HTML = "/index.html";

    private static final String INDEX_HTML_RESOURCE = "/" + RESOURCE_BASE + INDEX_HTML;

    private static final String CONFIG_JSON = "/config.json";

    private String apiOutsideUrl;

    private HttpContextRoot httpContextRoot;

    private ObjectMapper objectMapper;

    private String index;

    private final Map<String, HttpHandler> handlers = Map.of(
            INDEX_HTML, this::doGetIndex,
            CONFIG_JSON, this::doGetConfig
    );

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        try {
            this.index = loadIndex();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected String loadIndex() throws IOException {

        try(final var stream = getIndexInputStream()) {

            if (stream == null) {
                throw new IOException("index.html not found in " + INDEX_HTML_RESOURCE);
            }

            final var bytes = stream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        handlers.getOrDefault(req.getPathInfo(), super::doGet).handle(req, resp);
    }

    @Override
    protected void doNotFound(final HttpServletRequest req,
                              final HttpServletResponse resp,
                              final String encodedPathInContext) throws IOException {
        doGetIndex(req, resp);
    }

    protected void doGetIndex(final HttpServletRequest req,
                              final HttpServletResponse resp) throws IOException {

        var bytes = index.getBytes(StandardCharsets.UTF_8);
        resp.setContentLength(bytes.length);
        resp.setStatus(SC_OK);
        resp.setContentType("text/html; charset=UTF-8");
        resp.getOutputStream().write(bytes);
    }

    protected void doGetConfig(final HttpServletRequest req,
                               final HttpServletResponse resp) throws IOException {

        final var api = new WebUIApplicationApiConfiguration();
        api.setUrl(getApiOutsideUrl());

        final var config = new WebUIApplicationConfiguration();
        config.setApi(api);

        resp.setStatus(SC_OK);
        resp.setContentType("application/json; charset=UTF-8");

        try (var out = resp.getOutputStream()) {
            getObjectMapper().writeValue(out, config);
        }
    }

    private InputStream getIndexInputStream() throws IOException {

        var stream = WebUIReactServlet.class.getResourceAsStream(INDEX_HTML_RESOURCE);

        if (stream == null) {

            var path = getServletContext().getRealPath(INDEX_HTML);

            if (path != null) {
                return Files.newInputStream(Path.of(path));
            }
        }

        return stream;
    }

    public String getApiOutsideUrl() {
        return apiOutsideUrl;
    }

    @Inject
    public void setApiOutsideUrl(@Named(API_OUTSIDE_URL) String apiOutsideUrl) {
        this.apiOutsideUrl = apiOutsideUrl;
    }

    public HttpContextRoot getHttpContextRoot() {
        return httpContextRoot;
    }

    @Inject
    public void setHttpContextRoot(HttpContextRoot httpContextRoot) {
        this.httpContextRoot = httpContextRoot;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
