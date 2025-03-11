package dev.getelements.elements.rest.application;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.service.application.ApplicationConfigurationService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import static com.google.common.base.Strings.nullToEmpty;

/**
 * Created by patricktwohig on 7/13/15.
 */
@Path("application/{applicationNameOrId}/configuration")
public class ApplicationConfigurationResource {

    private ApplicationConfigurationService applicationConfigurationService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Searches application profiles",
            description =
                    "Searches all instances of ApplicationProfiles associated with  the application.  The " +
                    "search query may be a full text search.")
    public Pagination<ApplicationConfiguration> getApplicationProfiles(
            @PathParam("applicationNameOrId") final String applicationNameOrId,
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive summary.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive summary.");
        }

        final String query = nullToEmpty(search).trim();

        return query.isEmpty() ?
            getApplicationConfigurationService().getApplicationProfiles(applicationNameOrId, offset, count) :
            getApplicationConfigurationService().getApplicationProfiles(applicationNameOrId, offset, count, search);

    }

    @PUT
    @Path("{applicationConfigurationId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Updates the ProductBundle",
               description = "Updates the ProductBundle for the given ApplicationConfiguration")
    public ApplicationConfiguration updateProductBundleForApplicationConfiguration(

            @PathParam("applicationNameOrId")
            final String applicationNameOrId,

            @PathParam("applicationConfigurationId")
            final String applicationConfigurationId,

            final List<ProductBundle> productBundles
    ) {

        if (productBundles == null || productBundles.size() == 0) {
            throw new InvalidParameterException("ProductBundles must not be empty.");
        }

        final ApplicationConfiguration applicationConfiguration = getApplicationConfigurationService()
                .updateProductBundles(applicationConfigurationId, productBundles);

        return applicationConfiguration;
    }



    public ApplicationConfigurationService getApplicationConfigurationService() {
        return applicationConfigurationService;
    }

    @Inject
    public void setApplicationConfigurationService(ApplicationConfigurationService applicationConfigurationService) {
        this.applicationConfigurationService = applicationConfigurationService;
    }

}
