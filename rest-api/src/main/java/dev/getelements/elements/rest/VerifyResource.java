package dev.getelements.elements.rest;

import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.service.user.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Public endpoint that completes email verification by consuming a single-use token from a verification link.
 */
@Path("verify")
public class VerifyResource {

    private EmailVerificationService verificationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Complete Email Verification",
            description = "Completes email verification by consuming the single-use token from a verification link. "
                    + "Moves the associated UID status to VERIFIED.")
    public UserUid completeVerification(@QueryParam("token") final String token) {
        return getVerificationService().completeVerification(token);
    }

    public EmailVerificationService getVerificationService() {
        return verificationService;
    }

    @Inject
    @Named(UNSCOPED)
    public void setVerificationService(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

}
