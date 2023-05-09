package dev.getelements.elements.service.manifest;

import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.rt.Context;
import dev.getelements.elements.rt.manifest.http.HttpManifest;
import dev.getelements.elements.rt.manifest.model.ModelManifest;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;
import dev.getelements.elements.service.ManifestService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class ReadOnlyManifestService implements ManifestService {

    private Context.Factory contextFactory;

    @Override
    public HttpManifest getHttpManifestForApplication(Application application) {
        final Context context = getContextFactory().getContextForApplication(application.getId());
        return context.getManifestContext().getHttpManifest();
    }

    @Override
    public ModelManifest getModelManifestForApplication(Application application) {
        final Context context = getContextFactory().getContextForApplication(application.getId());
        return context.getManifestContext().getModelManifest();
    }

    @Override
    public SecurityManifest getSecurityManifestForApplication(Application application) {
        final Context context = getContextFactory().getContextForApplication(application.getId());
        return context.getManifestContext().getSecurityManifest();
    }

    public Context.Factory getContextFactory() {
        return contextFactory;
    }

    @Inject
    public void setContextFactory(Context.Factory contextFactory) {
        this.contextFactory = contextFactory;
    }

}
