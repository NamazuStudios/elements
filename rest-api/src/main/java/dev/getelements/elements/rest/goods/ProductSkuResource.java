package dev.getelements.elements.rest.goods;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.service.goods.ProductSkuService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("product/sku")
public class ProductSkuResource {

    private ProductSkuService productSkuService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a paginated list of Product SKUs.",
            description = "Returns all Product SKUs, optionally filtered by schema.")
    public Pagination<ProductSku> getProductSkus(
            @QueryParam("schema") String schema,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count) {

        if (schema != null && !schema.isBlank()) {
            return getProductSkuService().getProductSkus(schema, offset, count);
        }

        return getProductSkuService().getProductSkus(offset, count);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Product SKU by its database id.",
            description = "Returns the Product SKU identified by the given database id.")
    public ProductSku getProductSku(@PathParam("id") String id) {
        return getProductSkuService().getProductSku(id);
    }

    @GET
    @Path("{schema}/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Product SKU by schema and productId.",
            description = "Returns the Product SKU matching the given provider schema and product id.")
    public ProductSku getProductSkuBySchemaAndProductId(
            @PathParam("schema") String schema,
            @PathParam("productId") String productId) {
        return getProductSkuService().getProductSku(schema, productId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Product SKU.",
            description = "Creates a new Product SKU mapping a provider schema + productId to a set of rewards.")
    public Response createProductSku(final ProductSku productSku) {
        final ProductSku created = getProductSkuService().createProductSku(productSku);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an existing Product SKU.",
            description = "Updates the Product SKU identified by the given database id.")
    public ProductSku updateProductSku(@PathParam("id") String id, final ProductSku productSku) {
        return getProductSkuService().updateProductSku(productSku.withId(id));
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes a Product SKU.",
            description = "Permanently removes the Product SKU identified by the given database id.")
    public void deleteProductSku(@PathParam("id") String id) {
        getProductSkuService().deleteProductSku(id);
    }

    public ProductSkuService getProductSkuService() {
        return productSkuService;
    }

    @Inject
    public void setProductSkuService(ProductSkuService productSkuService) {
        this.productSkuService = productSkuService;
    }

}
