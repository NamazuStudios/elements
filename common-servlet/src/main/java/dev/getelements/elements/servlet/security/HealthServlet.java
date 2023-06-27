package dev.getelements.elements.servlet.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.exception.ErrorCode;
import dev.getelements.elements.model.health.HealthErrorResponse;
import dev.getelements.elements.service.HealthStatusService;
import dev.getelements.elements.service.Unscoped;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static dev.getelements.elements.model.health.HealthStatus.HEALTHY_THRESHOLD;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class HealthServlet extends HttpServlet {

    private ObjectMapper objectMapper;

    private HealthStatusService healthStatusService;

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {

        final var status = getHealthStatusService().checkHealthStatus();
        resp.setContentType("application/json");

        if (status.getOverallHealth() < HEALTHY_THRESHOLD) {
            final var errorResponse = new HealthErrorResponse();
            errorResponse.setMessage("Instance is Unhealthy.");
            errorResponse.setCode(ErrorCode.UNHEALTHY.toString());
            errorResponse.setHealthStatus(status);
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
            getObjectMapper().writeValue(resp.getOutputStream(), errorResponse);
        } else {
            resp.setStatus(SC_OK);
            getObjectMapper().writeValue(resp.getOutputStream(), status);
        }

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
