package com.namazustudios.socialengine.rest.security;

import com.namazustudios.socialengine.model.session.MockSessionCreation;
import com.namazustudios.socialengine.model.session.MockSessionRequest;
import com.namazustudios.socialengine.model.session.SessionCreation;
import com.namazustudios.socialengine.service.MockSessionService;
import com.namazustudios.socialengine.util.ValidationHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.swagger.EnhancedApiListingResource.SOCIALENGINE_SESSION_SECRET;

@Api(value = "Mock Sessions",
     description = "Creates mock sessions for running tests against the server.  This will generate valid sessions, " +
                   "profiles, and users which can be used for testing.  The system may opt to delete or destroy test " +
                   "users automatically after they have been generated.",
     authorizations = {@Authorization(SESSION_SECRET), @Authorization(SOCIALENGINE_SESSION_SECRET)})
@Path("mock_session")
public class MockSessionResource {

    private ValidationHelper validationHelper;

    private MockSessionService mockSessionService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Creates a Mock Session",
                  notes = "Begins a session by accepting a mock session request.  The request must be made with an " +
                          "authenticated super-user.")
    public MockSessionCreation createSession(final MockSessionRequest mockSessionRequest) {
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
