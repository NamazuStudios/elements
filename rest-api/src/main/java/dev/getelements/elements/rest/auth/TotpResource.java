package dev.getelements.elements.rest.auth;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.auth.TotpEnrollment;
import dev.getelements.elements.sdk.model.auth.TotpEnrollmentConfirmRequest;
import dev.getelements.elements.sdk.model.auth.TotpRecoveryCodes;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.service.auth.TotpAdminService;
import dev.getelements.elements.sdk.service.auth.TotpEnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * Self-service TOTP enrollment for the calling user's own account, plus a SUPERUSER-only reset path for
 * another account (lost device with no remaining recovery codes).
 */
@Path("totp")
public class TotpResource {

    private TotpEnrollmentService totpEnrollmentService;

    private TotpAdminService totpAdminService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Returns whether the calling user currently has TOTP enrollment confirmed and active.")
    public boolean isEnrolled() {
        return getTotpEnrollmentService().isEnrolled();
    }

    @POST
    @Path("enroll")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Begins TOTP enrollment for the calling user",
            description = "Generates a new shared secret and provisioning URI. Not yet enforced at login " +
                    "until confirmed with a valid code via enroll/confirm.")
    public TotpEnrollment beginEnrollment() {
        return getTotpEnrollmentService().beginEnrollment();
    }

    @POST
    @Path("enroll/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Confirms a pending TOTP enrollment",
            description = "Activates enforcement and returns a freshly-generated set of one-time recovery " +
                    "codes. The codes are never retrievable again after this response.")
    public TotpRecoveryCodes confirmEnrollment(final TotpEnrollmentConfirmRequest request) {
        return getTotpEnrollmentService().confirmEnrollment(request.getCode());
    }

    @DELETE
    @Operation(summary = "Disables and clears TOTP enrollment for the calling user's own account.")
    public void disableTotp() {
        getTotpEnrollmentService().disableTotp();
    }

    @DELETE
    @Path("{userId}")
    @Operation(
            summary = "Disables and clears TOTP enrollment for another user's account",
            description = "Requires SUPERUSER access. The account-recovery path for a lost authenticator " +
                    "device with no remaining recovery codes.")
    public void resetTotp(@PathParam("userId") String userId) {

        userId = Strings.nullToEmpty(userId).trim();

        if (userId.isEmpty()) {
            throw new NotFoundException();
        }

        getTotpAdminService().resetTotp(userId);

    }

    public TotpEnrollmentService getTotpEnrollmentService() {
        return totpEnrollmentService;
    }

    @Inject
    public void setTotpEnrollmentService(TotpEnrollmentService totpEnrollmentService) {
        this.totpEnrollmentService = totpEnrollmentService;
    }

    public TotpAdminService getTotpAdminService() {
        return totpAdminService;
    }

    @Inject
    public void setTotpAdminService(TotpAdminService totpAdminService) {
        this.totpAdminService = totpAdminService;
    }

}
