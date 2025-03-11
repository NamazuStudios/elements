package dev.getelements.elements.rest.security;

import dev.getelements.elements.sdk.model.session.MockSessionCreation;
import dev.getelements.elements.sdk.model.session.MockSessionRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.MockSessionService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("mock_session")
public class MockSessionResource {

    private ValidationHelper validationHelper;

    private MockSessionService mockSessionService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a Mock Session",
            description = "Begins a session by accepting a mock session request.  The request must be made with an " +
                          "authenticated super-user.")
    public MockSessionCreation createMockSession(final MockSessionRequest mockSessionRequest) {
        getValidationHelper().validateModel(mockSessionRequest);
        return getMockSessionService().createMockSession(mockSessionRequest);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MockSessionService getMockSessionService() {
        return mockSessionService;
    }

    @Inject
    public void setMockSessionService(MockSessionService mockSessionService) {
        this.mockSessionService = mockSessionService;
    }

}
