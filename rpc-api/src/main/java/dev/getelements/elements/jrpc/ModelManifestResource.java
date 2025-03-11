package dev.getelements.elements.jrpc;

import dev.getelements.elements.rt.ModelManifestService;
import dev.getelements.elements.rt.manifest.model.ModelManifest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("model/manifest")
public class ModelManifestResource {

    private ModelManifestService modelManifestService;

    @GET
    @Produces(APPLICATION_JSON)
    public ModelManifest getModelManifest() {
        return getModelManifestService().getModelManifest();
    }

    public ModelManifestService getModelManifestService() {
        return modelManifestService;
    }

    @Inject
    public void setModelManifestService(ModelManifestService modelManifestService) {
        this.modelManifestService = modelManifestService;
    }
}
