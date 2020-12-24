package com.namazustudios.socialengine.cdnserve.api;

import com.namazustudios.socialengine.model.Deployment;
import com.namazustudios.socialengine.model.Pagination;
import io.swagger.annotations.ApiOperation;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("deployment/{applicationId}")
public class DeploymentResource {

    private DeploymentService deploymentService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Pagination<Deployment> getDeployments(final @PathParam("applicationId") String applicationId) {
        return getDeploymentService().getDeployments(applicationId, 0, 100);
    }

    @GET
    @Path("{deploymentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Deployment getDeployment(final @PathParam("applicationId") String applicationId,
                                    final @PathParam("deploymentId") String deploymentId) {
        return getDeploymentService().getDeployment(applicationId, deploymentId);
    }

    @GET
    @Path("current")
    @Produces(MediaType.APPLICATION_JSON)
    public Deployment getCurrentDeployment(final @PathParam("applicationId") String applicationId) {
        return getDeploymentService().getCurrentDeployment(applicationId);
    }

    @POST
    @ApiOperation(value = "Create Deployment",
            notes = "This will create a new deployment if one does not exist, or update an existing deployment if a matching version exists." +
                    " This will search the git repo for the matching revision" +
                    "and clone all content to the static endpoint for delivery.")
    @Produces(MediaType.APPLICATION_JSON)
    public Deployment createNewDeployment(final @PathParam("applicationId") String applicationId,
                                          CreateDeploymentRequest createDeploymentRequest) {
        return getDeploymentService().createOrUpdateDeployment(applicationId, createDeploymentRequest);
    }

    @DELETE
    @Path("{deploymentId}")
    public void deleteDeployment(final @PathParam("applicationId") String applicationId,
                                 final @PathParam("deploymentId") String deploymentId) {
        getDeploymentService().deleteDeployment(applicationId, deploymentId);
    }

    private DeploymentService getDeploymentService() {
        return deploymentService;
    }

    @Inject
    private void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

}
