package dev.getelements.elements.rest.auth;

import com.google.common.base.Strings;
import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.auth.*;
import dev.getelements.elements.sdk.service.auth.OidcAuthSchemeService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;


@Path("auth_scheme/oidc")
public class OidcAuthSchemeResource {

    private OidcAuthSchemeService oidcAuthSchemeService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Lists all auth schemes in the system",
            description = "Requires SUPERUSER access. Gets a pagination of Auth Schemes for the given query.")
    public Pagination<OidcAuthScheme> getAuthSchemes(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("tags") final List<String> tags) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getOidcAuthSchemeService().getAuthSchemes(offset, count, tags);
    }

    @GET
    @Path("{oidcAuthSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets a specific Auth Scheme",
            description = "Gets a specific Auth Scheme by the oidcAuthSchemeId.")
    public OidcAuthScheme getAuthScheme(@PathParam("oidcAuthSchemeId") String oidcAuthSchemeId) {

        oidcAuthSchemeId = Strings.nullToEmpty(oidcAuthSchemeId).trim();

        if (oidcAuthSchemeId.isEmpty()) {
            throw new NotFoundException();
        }

        return getOidcAuthSchemeService().getAuthScheme(oidcAuthSchemeId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Creates a new Auth Scheme",
            description = "Creates a new Auth Scheme, from the data in the given auth scheme request")
    public CreateOrUpdateOidcAuthSchemeResponse createAuthScheme(final CreateOrUpdateOidcAuthSchemeRequest oidcAuthSchemeRequest) {
        return getOidcAuthSchemeService().createAuthScheme(oidcAuthSchemeRequest);
    }

    @PUT
    @Path("{oidcAuthSchemeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Updates an Auth Scheme",
            description = "Updates an Auth Scheme with the specified data in the auth scheme request.")
    public CreateOrUpdateOidcAuthSchemeResponse updateAuthScheme(
            @PathParam("oidcAuthSchemeId")
            final String oidcAuthSchemeId,
            final CreateOrUpdateOidcAuthSchemeRequest oidcAuthSchemeRequest) {
        return getOidcAuthSchemeService().updateAuthScheme(oidcAuthSchemeId, oidcAuthSchemeRequest);
    }

    @DELETE
    @Path("{oidcAuthSchemeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Deletes an Auth Scheme",
            description = "Deletes an Auth Scheme with the specified id.")
    public void deleteAuthScheme(@PathParam("oidcAuthSchemeId") String oidcAuthSchemeId) {

        oidcAuthSchemeId = Strings.nullToEmpty(oidcAuthSchemeId).trim();

        if (oidcAuthSchemeId.isEmpty()) {
            throw new NotFoundException();
        }

        getOidcAuthSchemeService().deleteAuthScheme(oidcAuthSchemeId);
    }

    public OidcAuthSchemeService getOidcAuthSchemeService() {
        return oidcAuthSchemeService;
    }

    @Inject
    public void setOidcAuthSchemeService(OidcAuthSchemeService oidcAuthSchemeService) {
        this.oidcAuthSchemeService = oidcAuthSchemeService;
    }
}
