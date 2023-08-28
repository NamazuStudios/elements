package dev.getelements.elements.cdnserve.api;

import dev.getelements.elements.model.Deployment;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.rest.AuthSchemes;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Api(value = "Deployment",
        description = "Manages content deployments for an app in the server.",
        authorizations = {@Authorization(AuthSchemes.AUTH_BEARER), @Authorization(AuthSchemes.SESSION_SECRET), @Authorization(AuthSchemes.SOCIALENGINE_SESSION_SECRET)})
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

    @PUT
    @Path("{version}")
    @ApiOperation(value = "Update Deployment by version",
            notes = "This will update the revision for a specific deployment version" +
                    " This will search the git repo for the matching revision" +
                    "and clone all content to the static endpoint for delivery.")
    @Produces(MediaType.APPLICATION_JSON)
    public Deployment updateDeployment(final @PathParam("applicationId") String applicationId,
                                          final @PathParam("version") String version,
                                          UpdateDeploymentRequest updateDeploymentRequest) {
        return getDeploymentService().updateDeployment(applicationId, version, updateDeploymentRequest);
    }

    @POST
    @ApiOperation(value = "Create Deployment",
            notes = "This will create a new deployment if one does not exist. It will throw an error if the requested version already exists" +
                    " This will search the git repo for the matching revision" +
                    "and clone all content to the static endpoint for delivery.")
    @Produces(MediaType.APPLICATION_JSON)
    public Deployment createNewDeployment(final @PathParam("applicationId") String applicationId,
                                          CreateDeploymentRequest createDeploymentRequest) {
        return getDeploymentService().createDeployment(applicationId, createDeploymentRequest);
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
