package dev.getelements.elements.rest.goods;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.ProductBundle;
import dev.getelements.elements.sdk.service.goods.ProductBundleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("product/bundle")
public class ProductBundleResource {

    private ProductBundleService productBundleService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a paginated list of Product Bundles.",
            description = "Returns all Product Bundles, optionally filtered by application and/or schema.")
    public Pagination<ProductBundle> getProductBundles(
            @QueryParam("applicationNameOrId") String applicationNameOrId,
            @QueryParam("schema") String schema,
            @QueryParam("productId") String productId,
            @QueryParam("tag") List<String> tags,
            @QueryParam("offset") @DefaultValue("0") int offset,
            @QueryParam("count") @DefaultValue("20") int count) {
        return getProductBundleService().getProductBundles(applicationNameOrId, schema, productId, tags, offset, count);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Product Bundle by its database id.",
            description = "Returns the Product Bundle identified by the given database id.")
    public ProductBundle getProductBundle(@PathParam("id") String id) {
        return getProductBundleService().getProductBundle(id);
    }

    @GET
    @Path("{applicationNameOrId}/{schema}/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Gets a Product Bundle by application, schema, and productId.",
            description = "Returns the Product Bundle matching the given application, provider schema and product id.")
    public ProductBundle getProductBundleByKey(
            @PathParam("applicationNameOrId") String applicationNameOrId,
            @PathParam("schema") String schema,
            @PathParam("productId") String productId) {
        return getProductBundleService().getProductBundle(applicationNameOrId, schema, productId);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Creates a new Product Bundle.",
            description = "Creates a new Product Bundle mapping an application + provider schema + productId to a set of rewards.")
    public Response createProductBundle(final ProductBundle bundle) {
        final ProductBundle created = getProductBundleService().createProductBundle(bundle);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Updates an existing Product Bundle.",
            description = "Updates the Product Bundle identified by the given database id.")
    public ProductBundle updateProductBundle(@PathParam("id") String id, final ProductBundle bundle) {
        bundle.setId(id);
        return getProductBundleService().updateProductBundle(bundle);
    }

    @DELETE
    @Path("{id}")
    @Operation(
            summary = "Deletes a Product Bundle.",
            description = "Permanently removes the Product Bundle identified by the given database id.")
    public void deleteProductBundle(@PathParam("id") String id) {
        getProductBundleService().deleteProductBundle(id);
    }

    public ProductBundleService getProductBundleService() {
        return productBundleService;
    }

    @Inject
    public void setProductBundleService(ProductBundleService productBundleService) {
        this.productBundleService = productBundleService;
    }

}
