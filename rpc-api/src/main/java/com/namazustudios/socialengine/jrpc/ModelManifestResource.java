package com.namazustudios.socialengine.jrpc;

import com.namazustudios.socialengine.rt.ModelManifestService;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;

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
