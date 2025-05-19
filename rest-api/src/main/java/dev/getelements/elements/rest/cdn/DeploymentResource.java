package dev.getelements.elements.rest.cdn;

import dev.getelements.elements.sdk.model.Deployment;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.cdn.CreateDeploymentRequest;
import dev.getelements.elements.sdk.model.cdn.UpdateDeploymentRequest;
import dev.getelements.elements.sdk.service.cdn.CdnDeploymentService;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("deployment/{applicationId}")
public class DeploymentResource {

    private CdnDeploymentService cdnDeploymentService;

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
    @Schema(description = "This will update the revision for a specific deployment version" +
                    " This will search the git repo for the matching revision" +
                    "and clone all content to the static endpoint for delivery.")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Deployment updateDeployment(

            @PathParam("applicationId")
            final String applicationId,

            @PathParam("version")
            final String version,

            final UpdateDeploymentRequest updateDeploymentRequest) {
        return getDeploymentService().updateDeployment(applicationId, version, updateDeploymentRequest);
    }

    @POST
    @Schema(description = "This will create a new deployment if one does not exist. It will throw an error if the " +
            "requested version already exists This will search the git repo for the matching revision" +
            "and clone all content to the static endpoint for delivery.")
    @Consumes(MediaType.APPLICATION_JSON)
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

    private CdnDeploymentService getDeploymentService() {
        return cdnDeploymentService;
    }

    @Inject
    private void setDeploymentService(CdnDeploymentService cdnDeploymentService) {
        this.cdnDeploymentService = cdnDeploymentService;
    }

}
