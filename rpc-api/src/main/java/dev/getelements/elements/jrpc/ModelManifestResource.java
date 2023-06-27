package dev.getelements.elements.jrpc;

import dev.getelements.elements.rt.ModelManifestService;
import dev.getelements.elements.rt.manifest.model.ModelManifest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
