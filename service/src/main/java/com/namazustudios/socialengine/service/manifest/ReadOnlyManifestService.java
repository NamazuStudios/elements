package com.namazustudios.socialengine.service.manifest;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;
import com.namazustudios.socialengine.service.ManifestService;

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
