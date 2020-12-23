package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;

import javax.inject.Inject;

public class DefaultManifestDao implements ManifestDao {

    private Context.Factory contextFactory;

    @Override
    public HttpManifest getHttpManifestForApplication(final Application application) {
        final Context context = getContextFactory().getContextForApplicationUniqueId(application.getId());
        return context.getManifestContext().getHttpManifest();
    }

    @Override
    public ModelManifest getModelManifestForApplication(final Application application) {
        final Context context = getContextFactory().getContextForApplicationUniqueId(application.getId());
        return context.getManifestContext().getModelManifest();
    }

    @Override
    public SecurityManifest getSecurityManifestForApplication(final Application application) {
        final Context context = getContextFactory().getContextForApplicationUniqueId(application.getId());
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
