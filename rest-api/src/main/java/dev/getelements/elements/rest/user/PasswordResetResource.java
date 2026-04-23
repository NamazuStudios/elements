package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.user.CompletePasswordResetRequest;
import dev.getelements.elements.sdk.model.user.PasswordResetRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.user.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

/**
 * Public REST resource for the self-service password reset flow.
 * Neither endpoint requires authentication.
 */
@Path("user/password/reset")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PasswordResetResource {

    private PasswordResetService passwordResetService;

    private ValidationHelper validationHelper;

    private String resetBaseUrl;

    @POST
    @Path("request")
    @Operation(
            summary = "Request Password Reset",
            description = "Sends a password reset link to the given email address if an account exists. "
                    + "Always returns 200 to prevent user enumeration.")
    public void requestReset(final PasswordResetRequest request,
                             @Context final UriInfo uriInfo) {

        getValidationHelper().validateModel(request);

        final var baseUrl = resolveBaseUrl(uriInfo);
        getPasswordResetService().requestReset(request.getEmail(), baseUrl);
    }

    @POST
    @Path("complete")
    @Operation(
            summary = "Complete Password Reset",
            description = "Validates the reset token and sets the new password. "
                    + "All existing sessions are invalidated. Returns 400 if the token is invalid or expired.")
    public void completeReset(final CompletePasswordResetRequest request) {

        getValidationHelper().validateModel(request);

        getPasswordResetService().completeReset(request.getToken(), request.getPassword());
    }

    private String resolveBaseUrl(final UriInfo uriInfo) {

        if (resetBaseUrl != null && !resetBaseUrl.isBlank()) {
            return resetBaseUrl;
        }

        return uriInfo.getBaseUriBuilder().path("user/password/reset/complete").build().toString();
    }

    public PasswordResetService getPasswordResetService() {
        return passwordResetService;
    }

    @Inject
    public void setPasswordResetService(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public String getResetBaseUrl() {
        return resetBaseUrl;
    }

    @Inject
    @Named(PasswordResetService.RESET_BASE_URL)
    public void setResetBaseUrl(String resetBaseUrl) {
        this.resetBaseUrl = resetBaseUrl;
    }

}
