package com.namazustudios.socialengine.codeserve.api.deploy;

import com.namazustudios.socialengine.model.Pagination;

import javax.ws.rs.*;

@Path("deployment/{applicationId}")
public class DeploymentResource {

    @POST
    public Deployment createNewDeployment(final CreateDeploymentRequest createDeploymentRequest) {
        return null;
    }

    @GET
    public Pagination<Deployment> getDeployments(final @PathParam("applicationId") String applicationId) {
        return null;
    }

    @GET
    @Path("{deploymentId}")
    public Deployment getDeployment(final @PathParam("applicationId") String applicationId,
                                    final @PathParam("deploymentId") String id) {
        return null;
    }

    @GET
    @Path("current")
    public Pagination<Deployment> getCurrentDeployment(final @PathParam("applicationId") String applicationId) {
        return null;
    }

    @DELETE
    @Path("{deploymentId}")
    public void deleteDeployment(final @PathParam("applicationId") String applicationId,
                                 final @PathParam("deploymentId") String id) {

    }

}
