package dev.getelements.elements.rest.index;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.index.BuildIndexRequest;
import dev.getelements.elements.model.index.IndexPlan;
import dev.getelements.elements.rest.AuthSchemes;
import dev.getelements.elements.service.IndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Api(value = "Indexes",
        description = "A RESTful Interface used to build and manage custom indexes in the database.",
        authorizations = {
                @Authorization(AuthSchemes.AUTH_BEARER),
                @Authorization(AuthSchemes.SESSION_SECRET),
                @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)
})
@Path("index")
public class IndexResource {

    private IndexService indexService;

    @GET
    @Path("plan")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Builds all indexes.")
    public Pagination<IndexPlan<?>> getPlans(
            @QueryParam("offset") @DefaultValue("0") final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count) {
        return getIndexService().getPlans(offset, count);
    }

    @POST
    @Path("build")
    @ApiOperation(value = "Builds all indexes.")
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
