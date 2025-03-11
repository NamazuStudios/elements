package dev.getelements.elements.servlet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.model.Version;

import dev.getelements.elements.sdk.service.version.VersionService;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;

/**
 * Defers to the {@link VersionService} to read the build metadata, and respond with a {@link Version} instance
 * using the supplied {@link ObjectMapper}.
 */
public class VersionServlet extends HttpServlet {

    private ObjectMapper objectMapper;

    private VersionService versionService;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        final Version version = getVersionService().getVersion();
        resp.setStatus(SC_OK);
        resp.setContentType("application/json");
        getObjectMapper().writeValue(resp.getOutputStream(), version);
        super.doGet(req, resp);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public VersionService getVersionService() {
        return versionService;
    }

    @Inject
    public void setVersionService(VersionService versionService) {
        this.versionService = versionService;
    }

}
