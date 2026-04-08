package dev.getelements.elements.rest.user;

import dev.getelements.elements.sdk.model.user.EmailVerificationRequest;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.user.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

/**
 * REST resource for initiating email verification.
 * Mounted under {@code /user}.
 */
@Path("user")
public class UserEmailVerificationResource {

    private EmailVerificationService verificationService;

    private ValidationHelper validationHelper;

    private String verificationBaseUrl;

    @POST
    @Path("me/email/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Request Email Verification",
            description = "Sends a verification link to the given email address associated with the current user.")
    public UserUid requestVerification(final EmailVerificationRequest request,
                                       @Context final UriInfo uriInfo) {
        getValidationHelper().validateModel(request);
        final var baseUrl = resolveBaseUrl(uriInfo);
        return getVerificationService().requestVerification(request.getEmail(), baseUrl);
    }

    private String resolveBaseUrl(final UriInfo uriInfo) {
        if (verificationBaseUrl != null && !verificationBaseUrl.isBlank()) {
            return verificationBaseUrl;
        }
        return uriInfo.getBaseUriBuilder().path("verify").build().toString();
    }

    public EmailVerificationService getVerificationService() {
        return verificationService;
    }

    @Inject
    public void setVerificationService(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public String getVerificationBaseUrl() {
        return verificationBaseUrl;
    }

    @Inject
    @Named(EmailVerificationService.VERIFICATION_BASE_URL)
    public void setVerificationBaseUrl(String verificationBaseUrl) {
        this.verificationBaseUrl = verificationBaseUrl;
    }

}
