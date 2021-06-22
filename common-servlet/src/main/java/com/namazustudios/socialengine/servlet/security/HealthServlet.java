package com.namazustudios.socialengine.servlet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.service.HealthStatusService;
import com.namazustudios.socialengine.service.Unscoped;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.namazustudios.socialengine.model.health.HealthStatus.HEALTHY_THRESHOLD;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class HealthServlet extends HttpServlet {

    private ObjectMapper objectMapper;

    private HealthStatusService healthStatusService;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {
        final var status = getHealthStatusService().checkHealthStatus();
        resp.setStatus(status.getOverallHealth() < HEALTHY_THRESHOLD ? SC_INTERNAL_SERVER_ERROR : SC_OK);
        resp.setContentType("application/json");
        getObjectMapper().writeValue(resp.getOutputStream(), status);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public HealthStatusService getHealthStatusService() {
        return healthStatusService;
    }

    @Inject
    public void setHealthStatusService(@Unscoped HealthStatusService healthStatusService) {
        this.healthStatusService = healthStatusService;
    }

}
