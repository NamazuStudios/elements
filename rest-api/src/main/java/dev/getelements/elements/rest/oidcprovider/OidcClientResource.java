package dev.getelements.elements.rest.oidcprovider;

import dev.getelements.elements.sdk.model.auth.OidcClientRegistration;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.auth.OidcClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

/**
 * Admin-facing management of downstream OIDC client registrations. Superuser-scoped: the service interface is
 * bound to the superuser implementation by the request-scope machinery.
 */
@Path("oidc_client")
public class OidcClientResource {

    private ValidationHelper validationHelper;

    private OidcClientService oidcClientService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lists all OIDC client registrations.")
    public List<OidcClientRegistration> getClients() {
        return getOidcClientService().getClients();
    }

    @GET
    @Path("{clientId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Fetches a single OIDC client registration.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The registration."),
            @ApiResponse(responseCode = "404", description = "No registration with the supplied client id.")
    })
    public OidcClientRegistration getClient(@Parameter(description = "The client id")
                                            @PathParam("clientId") final String clientId) {
        return getOidcClientService().getClient(clientId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates an OIDC client registration.",
            description = "Generates the client id and secret when not supplied. The raw secret is echoed in " +
                    "this response only; it is stored hashed and can never be read back.")
    public OidcClientRegistration createClient(final OidcClientRegistration registration) {
        getValidationHelper().validateModel(registration);
        return getOidcClientService().createClient(registration);
    }

    @PUT
    @Path("{clientId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an OIDC client registration.",
            description = "A non-null clientSecret rotates the stored secret (echoed in this response only); " +
                    "a null clientSecret retains the existing one.")
    public OidcClientRegistration updateClient(@Parameter(description = "The client id")
                                               @PathParam("clientId") final String clientId,
                                               final OidcClientRegistration registration) {
        getValidationHelper().validateModel(registration);
        registration.setClientId(clientId);
        return getOidcClientService().updateClient(registration);
    }

    @DELETE
    @Path("{clientId}")
    @Operation(summary = "Deletes an OIDC client registration.")
    public void deleteClient(@Parameter(description = "The client id")
                             @PathParam("clientId") final String clientId) {
        getOidcClientService().deleteClient(clientId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public OidcClientService getOidcClientService() {
        return oidcClientService;
    }

    @Inject
    public void setOidcClientService(OidcClientService oidcClientService) {
        this.oidcClientService = oidcClientService;
    }

}
