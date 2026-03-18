package dev.getelements.elements.rest.goods;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import dev.getelements.elements.sdk.service.goods.ProductSkuSchemaService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("product/sku/schema")
public class ProductSkuSchemaResource {

    private ProductSkuSchemaService productSkuSchemaService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a paginated list of Product SKU Schemas.",
            description = "Returns all registered payment-provider schema identifiers.")
    public Pagination<ProductSkuSchema> getProductSkuSchemas(
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count) {
        return getProductSkuSchemaService().getProductSkuSchemas(offset, count);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Product SKU Schema by id.",
            description = "Returns the schema identifier with the given database id.")
    public ProductSkuSchema getProductSkuSchema(@PathParam("id") String id) {
        return getProductSkuSchemaService().getProductSkuSchema(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Product SKU Schema.",
            description = "Registers a payment-provider schema identifier. Returns an existing record if the schema value already exists.")
    public Response createProductSkuSchema(final ProductSkuSchema productSkuSchema) {
        final ProductSkuSchema created = getProductSkuSchemaService().createProductSkuSchema(productSkuSchema);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes a Product SKU Schema.",
            description = "Permanently removes the schema identifier with the given database id.")
    public void deleteProductSkuSchema(@PathParam("id") String id) {
        getProductSkuSchemaService().deleteProductSkuSchema(id);
    }

    public ProductSkuSchemaService getProductSkuSchemaService() {
        return productSkuSchemaService;
    }

    @Inject
    public void setProductSkuSchemaService(ProductSkuSchemaService productSkuSchemaService) {
        this.productSkuSchemaService = productSkuSchemaService;
    }

}
