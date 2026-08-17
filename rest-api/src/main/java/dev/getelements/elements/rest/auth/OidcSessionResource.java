package dev.getelements.elements.rest.auth;

import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptConfirmRequest;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptRequest;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptResponse;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptState;
import dev.getelements.elements.sdk.model.session.OidcLoginAttemptStatusResponse;
import dev.getelements.elements.sdk.model.session.OidcSessionRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcAuthService;
import dev.getelements.elements.sdk.service.auth.OidcLoginAttemptService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Provider-agnostic browser-redirect OIDC login for thick clients that cannot receive a browser redirect
 * directly. See {@link OidcCallbackResource} for the provider-facing half of the flow.
 */
@Path("oidc/session")
public class OidcSessionResource {

    private ValidationHelper validationHelper;

    private OidcLoginAttemptService oidcLoginAttemptService;

    private OidcAuthService oidcAuthService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Begins an OIDC login attempt, or validates a possessed id_token directly",
            description = "Supplying only 'provider' starts a pending browser-redirect login attempt and " +
                    "returns an id plus the authorize URL to open in the system browser. Additionally " +
                    "supplying 'idToken' instead validates it directly and returns a completed session " +
                    "synchronously, sharing validation code with the callback path.")
    public Response createOidcSession(final OidcLoginAttemptRequest request) {

        getValidationHelper().validateModel(request);

        if (!isNullOrEmpty(request.getIdToken())) {

            final var oidcSessionRequest = new OidcSessionRequest();
            oidcSessionRequest.setJwt(request.getIdToken());

            final var sessionCreation = getOidcAuthService().createSession(oidcSessionRequest);
            final var body = OidcLoginAttemptResponse.complete(sessionCreation);

            return Response.ok(body).build();

        }

        final var begin = getOidcLoginAttemptService().begin(request.getProvider());
        final var body = OidcLoginAttemptResponse.pending(
                begin.getId(), begin.getAuthorizeUrl(), begin.getExpiresAt(), begin.getConfirmToken());

        return Response.status(Response.Status.CREATED).entity(body).build();

    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Polls a pending OIDC login attempt",
            description = "Returns COMPLETE with the session exactly once, on the poll that first observes " +
                    "completion; a subsequent poll for the same id, or a poll for an unknown/expired " +
                    "id, returns 404. An account-linking attempt cannot be finalized this way -- once its " +
                    "external identity is validated, this returns an error directing the caller to " +
                    "POST {id}/confirm instead.")
    public OidcLoginAttemptStatusResponse pollOidcSession(@PathParam("id") final String id) {

        final var status = getOidcLoginAttemptService().poll(id);

        if (status.getStatus() == OidcLoginAttemptState.EXPIRED) {
            throw new NotFoundException();
        }

        return status;

    }

    @POST
    @Path("{id}/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Finalizes an account-linking OIDC login attempt",
            description = "Presents the confirmToken returned in the original POST /oidc/session response for " +
                    "this attempt, completing the deferred account-link mutation and returning the resulting " +
                    "session. Only applicable to a linking attempt; not needed for an anonymous one.")
    public OidcLoginAttemptStatusResponse confirmOidcSessionLink(
            @PathParam("id") final String id,
            final OidcLoginAttemptConfirmRequest request) {

        getValidationHelper().validateModel(request);

        final var status = getOidcLoginAttemptService().confirmLink(id, request.getConfirmToken());

        if (status.getStatus() == OidcLoginAttemptState.EXPIRED) {
            throw new NotFoundException();
        }

        return status;

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public OidcLoginAttemptService getOidcLoginAttemptService() {
        return oidcLoginAttemptService;
    }

    @Inject
    public void setOidcLoginAttemptService(OidcLoginAttemptService oidcLoginAttemptService) {
        this.oidcLoginAttemptService = oidcLoginAttemptService;
    }

    public OidcAuthService getOidcAuthService() {
        return oidcAuthService;
    }

    @Inject
    public void setOidcAuthService(OidcAuthService oidcAuthService) {
        this.oidcAuthService = oidcAuthService;
    }

}
