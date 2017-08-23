package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.ShutdownHooks;
import com.namazustudios.socialengine.dao.ManifestDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * Loads the various Manifest instances directly from the underlying RT Services.
 *
 * Created by patricktwohig on 8/14/17.
 */
public class RTManifestDao implements ManifestDao {

    private static final Logger logger = LoggerFactory.getLogger(RTManifestDao.class);

    private static final ShutdownHooks hooks = new ShutdownHooks(RTManifestDao.class);

    private Function<Application, ManifestLoader> applicationManifestLoaderFunction;

    private final ConcurrentMap<String, ManifestLoader> loaderCache = new ConcurrentHashMap<>();

    @Override
    public HttpManifest getHttpManifestForApplication(final Application application) {
        final ManifestLoader manifestLoader = getLoaderForApplication(application);
        return manifestLoader.getHttpManifest();
    }

    @Override
    public ModelManifest getModelManifestForApplication(final Application application) {
        final ManifestLoader manifestLoader = getLoaderForApplication(application);
        return manifestLoader.getModelManifest();
    }

    private ManifestLoader getLoaderForApplication(final Application application) {
        return loaderCache.computeIfAbsent(application.getId(), applicationId -> {
            final ManifestLoader manifestLoader = getApplicationManifestLoaderFunction().apply(application);
            hooks.add(manifestLoader, manifestLoader::close);
            logger.info("Creating manifest loader for {} ({}).", application.getName(), application.getId());
            return manifestLoader;
        });
    }

    public Function<Application, ManifestLoader> getApplicationManifestLoaderFunction() {
        return applicationManifestLoaderFunction;
    }

    @Inject
    public void setApplicationManifestLoaderFunction(Function<Application, ManifestLoader> applicationManifestLoaderFunction) {
        this.applicationManifestLoaderFunction = applicationManifestLoaderFunction;
    }

}
