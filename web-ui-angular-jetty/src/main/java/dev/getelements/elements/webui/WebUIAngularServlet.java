package dev.getelements.elements.webui;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.servlet.HttpContextRoot;
import dev.getelements.elements.servlet.HttpHandler;
import org.eclipse.jetty.servlet.DefaultServlet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import static dev.getelements.elements.Constants.API_OUTSIDE_URL;
import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class WebUIAngularServlet extends DefaultServlet {

    private static final String BASE_TAG = Pattern.quote("<base href=\"/\">");

    private static final String INDEX_HTML = "/index.html";

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

        final var resource = getResource(INDEX_HTML);

        try (var input = resource.getInputStream()) {
            final var bytes = input.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }

    }

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        handlers.getOrDefault(req.getPathInfo(), this::doGetDefault).handle(req, resp);
    }

    protected void doGetIndex(final HttpServletRequest req,
                              final HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(SC_OK);
        resp.getWriter().print(index);
    }

    protected void doGetConfig(final HttpServletRequest req,
                               final HttpServletResponse resp) throws ServletException, IOException {

        final var api = new WebUIApplicationApiConfiguration();
        api.setUrl(getApiOutsideUrl());

        final var config = new WebUIApplicationConfiguration();
        config.setApi(api);

        final var json = getObjectMapper().writeValueAsString(config);

        resp.setStatus(SC_OK);
        resp.setContentType("application/json");
        resp.getWriter().print(json);

    }

    protected void doGetDefault(final HttpServletRequest req,
                                final HttpServletResponse resp) throws ServletException, IOException {

        final var resource = getResource(req.getPathInfo());

        if (resource.exists()) {
            super.doGet(req, resp);
        } else {
            doGetIndex(req, resp);
        }

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
