package dev.getelements.elements.rest.iap;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.service.iap.IapSkuService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("iap/sku")
public class IapSkuResource {

    private IapSkuService iapSkuService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a paginated list of IAP SKUs.",
            description = "Returns all IAP SKUs, optionally filtered by schema.")
    public Pagination<IapSku> getIapSkus(
            @QueryParam("schema") String schema,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count) {

        if (schema != null && !schema.isBlank()) {
            return getIapSkuService().getIapSkus(schema, offset, count);
        }

        return getIapSkuService().getIapSkus(offset, count);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets an IAP SKU by its database id.",
            description = "Returns the IAP SKU identified by the given database id.")
    public IapSku getIapSku(@PathParam("id") String id) {
        return getIapSkuService().getIapSku(id);
    }

    @GET
    @Path("{schema}/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets an IAP SKU by schema and productId.",
            description = "Returns the IAP SKU matching the given provider schema and product id.")
    public IapSku getIapSkuBySchemaAndProductId(
            @PathParam("schema") String schema,
            @PathParam("productId") String productId) {
        return getIapSkuService().getIapSku(schema, productId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new IAP SKU.",
            description = "Creates a new IAP SKU mapping a provider schema + productId to a set of rewards.")
    public Response createIapSku(final IapSku iapSku) {
        final IapSku created = getIapSkuService().createIapSku(iapSku);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an existing IAP SKU.",
            description = "Updates the IAP SKU identified by the given database id.")
    public IapSku updateIapSku(@PathParam("id") String id, final IapSku iapSku) {
        return getIapSkuService().updateIapSku(iapSku.withId(id));
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes an IAP SKU.",
            description = "Permanently removes the IAP SKU identified by the given database id.")
    public void deleteIapSku(@PathParam("id") String id) {
        getIapSkuService().deleteIapSku(id);
    }

    public IapSkuService getIapSkuService() {
        return iapSkuService;
    }

    @Inject
    public void setIapSkuService(IapSkuService iapSkuService) {
        this.iapSkuService = iapSkuService;
    }

}
