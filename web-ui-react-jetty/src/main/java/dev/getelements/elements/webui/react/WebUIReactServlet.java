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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static java.lang.String.format;

public class WebUIReactServlet extends StaticContentServlet {

    private static final String BASE_TAG = Pattern.quote("<base href=\"/\">");

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
            final var replacement =  format("<base href=\"%s/\">", getHttpContextRoot().normalize("admin"));
            this.index = loadIndex().replaceAll(BASE_TAG, replacement);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected String loadIndex() throws IOException {
        try (var input = WebUIReactServlet.class.getResourceAsStream(INDEX_HTML_RESOURCE)) {
            final var bytes = input.readAllBytes();
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
        resp.setStatus(SC_OK);
        resp.getWriter().print(index);
    }

    protected void doGetConfig(final HttpServletRequest req,
                               final HttpServletResponse resp) throws IOException {

        final var api = new WebUIApplicationApiConfiguration();
        api.setUrl(getApiOutsideUrl());

        final var config = new WebUIApplicationConfiguration();
        config.setApi(api);

        final var json = getObjectMapper().writeValueAsString(config);

        resp.setStatus(SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().print(json);

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
