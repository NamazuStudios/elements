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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    /**
     * Begins the browser-redirect flow (only {@code provider} supplied), returning a pending attempt with a
     * poll {@code id}, the provider's {@code authorizeUrl}, and a {@code confirmToken}; or, if {@code idToken} is
     * also supplied, validates it directly and returns a completed session synchronously, skipping the
     * browser/poll steps entirely. If the caller already holds an Elements session, a pending attempt links the
     * external identity to that user on success instead of creating one — see
     * {@link OidcLoginAttemptService#begin}.
     *
     * @param request the provider to begin an attempt for, and/or an already-possessed id_token
     * @return {@code 201} with the pending attempt, or {@code 200} with the completed session
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Begins an OIDC login attempt, or validates a possessed id_token directly",
            description = "Supplying only 'provider' starts a pending browser-redirect login attempt and " +
                    "returns an id plus the authorize URL to open in the system browser. Additionally " +
                    "supplying 'idToken' instead validates it directly and returns a completed session " +
                    "synchronously, sharing validation code with the callback path.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "A pending browser-redirect attempt was created."),
            @ApiResponse(responseCode = "200",
                    description = "The supplied idToken was valid; the completed session is returned."),
            @ApiResponse(responseCode = "400", description = "The request failed validation, or is missing both " +
                    "provider and idToken.")
    })
    public Response createOidcSession(final OidcLoginAttemptRequest request) {

        getValidationHelper().validateModel(request);

        if (!isNullOrEmpty(request.getIdToken())) {

            final var oidcSessionRequest = new OidcSessionRequest();
            oidcSessionRequest.setJwt(request.getIdToken());
            oidcSessionRequest.setApplicationNameOrId(request.getApplicationNameOrId());

            final var sessionCreation = getOidcAuthService().createSession(oidcSessionRequest);
            final var body = OidcLoginAttemptResponse.complete(sessionCreation);

            return Response.ok(body).build();

        }

        final var begin = getOidcLoginAttemptService().begin(request.getProvider(), request.getApplicationNameOrId());
        final var body = OidcLoginAttemptResponse.pending(
                begin.getId(), begin.getAuthorizeUrl(), begin.getExpiresAt(), begin.getConfirmToken());

        return Response.status(Response.Status.CREATED).entity(body).build();

    }

    /**
     * Polls a pending attempt by id. Returns {@code COMPLETE} with the session exactly once, on the poll that
     * first observes completion. A linking attempt is never finalizable this way — once its external identity is
     * validated it becomes link-ready, and this throws {@link dev.getelements.elements.sdk.model.exception.ForbiddenException}
     * directing the caller to {@link #confirmOidcSessionLink} instead. See {@link OidcLoginAttemptService#poll}.
     *
     * @param id the opaque poll id returned by {@link #createOidcSession}
     * @return the current status of the attempt
     */
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The current PENDING, COMPLETE, or FAILED status."),
            @ApiResponse(responseCode = "404",
                    description = "The id is unknown, already consumed, or has expired."),
            @ApiResponse(responseCode = "403", description = "This is a linking attempt whose external identity " +
                    "has already been validated; call POST {id}/confirm instead.")
    })
    public OidcLoginAttemptStatusResponse pollOidcSession(
            @PathParam("id")
            @Parameter(description = "The opaque poll id returned by POST /oidc/session.")
            final String id) {

        final var status = getOidcLoginAttemptService().poll(id);

        if (status.getStatus() == OidcLoginAttemptState.EXPIRED) {
            throw new NotFoundException();
        }

        return status;

    }

    /**
     * Finalizes a linking attempt by presenting the {@code confirmToken} returned only in the original
     * {@link #createOidcSession} response for this attempt — the actual account-link mutation, deferred out of
     * the provider callback because it cannot verify it's talking to the same party that began the attempt. Not
     * applicable to (and never needed for) an anonymous attempt. See {@link OidcLoginAttemptService#confirmLink}.
     *
     * @param id the opaque poll id
     * @param request the confirmToken returned by {@link #createOidcSession} for this attempt
     * @return the completed session
     */
    @POST
    @Path("{id}/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Finalizes an account-linking OIDC login attempt",
            description = "Presents the confirmToken returned in the original POST /oidc/session response for " +
                    "this attempt, completing the deferred account-link mutation and returning the resulting " +
                    "session. Only applicable to a linking attempt; not needed for an anonymous one.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The link succeeded; the completed session is " +
                    "returned. Like the poll endpoint, this is returned exactly once."),
            @ApiResponse(responseCode = "404", description = "The id is unknown, not awaiting confirmation, " +
                    "already claimed, or expired."),
            @ApiResponse(responseCode = "403", description = "confirmToken is missing or doesn't match. Not " +
                    "consumed; retry with the correct token."),
            @ApiResponse(responseCode = "400", description = "The request failed validation (confirmToken blank).")
    })
    public OidcLoginAttemptStatusResponse confirmOidcSessionLink(
            @PathParam("id")
            @Parameter(description = "The opaque poll id returned by POST /oidc/session.")
            final String id,
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
