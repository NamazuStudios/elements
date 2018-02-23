package com.namazustudios.socialengine.servlet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.service.VersionService;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_OK;

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
