package dev.getelements.elements.rest.index;

import dev.getelements.elements.sdk.model.exception.InvalidParameterException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.index.BuildIndexRequest;
import dev.getelements.elements.sdk.model.index.IndexPlan;
import dev.getelements.elements.sdk.service.index.IndexService;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("index")
public class IndexResource {

    private IndexService indexService;

    @GET
    @Path("plan")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Gets all index plans.")
    public Pagination<IndexPlan<?>> getPlans(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {

        if (offset < 0) {
            throw new InvalidParameterException("Offset must have positive value.");
        }

        if (count < 0) {
            throw new InvalidParameterException("Count must have positive value.");
        }

        return getIndexService().getPlans(offset, count);

    }

    @POST
    @Path("build")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation( summary = "Builds all indexes.")
    public void buildIndexes(final BuildIndexRequest buildIndexRequest) {
        getIndexService().build(buildIndexRequest);
    }

    public IndexService getIndexService() {
        return indexService;
    }

    @Inject
    public void setIndexService(IndexService indexService) {
        this.indexService = indexService;
    }

}
