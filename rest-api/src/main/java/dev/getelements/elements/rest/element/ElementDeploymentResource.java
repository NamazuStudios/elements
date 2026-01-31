package dev.getelements.elements.rest.element;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.system.CreateElementDeploymentRequest;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.UpdateElementDeploymentRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.getelements.elements.sdk.service.system.ElementDeploymentService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

@Path("elements/deployment")
public class ElementDeploymentResource {

    private ValidationHelper validationHelper;

    private ElementDeploymentService deploymentService;

    @POST
    public ElementDeployment createElementDeployment(final CreateElementDeploymentRequest request) {
        getValidationHelper().validateModel(request);
        return getDeploymentService().createElementDeployment(request);
    }

    @GET
    public Pagination<ElementDeployment> getElementDeployments(
            @QueryParam("offset") @DefaultValue("0")  final int offset,
            @QueryParam("count")  @DefaultValue("20") final int count,
            @QueryParam("search") final String search) {
        return getDeploymentService().getElementDeployments(offset, count, search);
    }

    @GET
    @Path("{deploymentId}")
    public ElementDeployment getElementDeployment(
            @PathParam("deploymentId")
            final String deploymentId) {
        return getDeploymentService().getElementDeployment(deploymentId);
    }

    @PUT
    @Path("{deploymentId}")
    public ElementDeployment updateElementDeployment(
            @PathParam("deploymentId")
            final String deploymentId,
            final UpdateElementDeploymentRequest request) {
        getValidationHelper().validateModel(request);
        return getDeploymentService().updateElementDeployment(deploymentId, request);
    }

    @DELETE
    @Path("{deploymentId}")
    public void deleteElementDeployment(
            @PathParam("deploymentId")
            final String deploymentId) {
        getDeploymentService().deleteDeployment(deploymentId);
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public ElementDeploymentService getDeploymentService() {
        return deploymentService;
    }

    @Inject
    public void setDeploymentService(ElementDeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

}
