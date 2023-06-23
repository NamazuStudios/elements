package dev.getelements.elements.webui;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.servlet.HttpContextRoot;
import dev.getelements.elements.servlet.HttpHandler;
import org.eclipse.jetty.servlet.DefaultServlet;

import javax.inject.Inject;
import javax.inject.Named;
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

    private String apiOutsideUrl;

    private HttpContextRoot httpContextRoot;

    private ObjectMapper objectMapper;

    private final Map<String, HttpHandler> handlers = Map.of(
            "/index.html", this::doGetIndex,
            "/config.json", this::doGetConfig
    );

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        handlers.getOrDefault(req.getPathInfo(), super::doGet).handle(req, resp);
    }

    protected void doGetIndex(final HttpServletRequest req,
                              final HttpServletResponse resp) throws ServletException, IOException {

        final var replacement =  format("<base href=\"%s\">", getHttpContextRoot().normalize("web-ui"));
        final var index = loadIndex(req).replaceAll(BASE_TAG, replacement);

        resp.setStatus(SC_OK);
        resp.getWriter().print(index);

    }

    protected String loadIndex(final HttpServletRequest req) throws IOException {

        final var resource = getResource(req.getPathInfo());

        try (var input = resource.getInputStream()) {
            final var bytes = input.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }

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
