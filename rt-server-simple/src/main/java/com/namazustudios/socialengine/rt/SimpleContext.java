package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.manifest.startup.StartupManifest;
import com.namazustudios.socialengine.rt.manifest.startup.StartupModule;
import com.namazustudios.socialengine.rt.manifest.startup.StartupOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.Runtime.getRuntime;

@Singleton
public class SimpleContext implements Context {

    private Scheduler scheduler;

    private SchedulerContext schedulerContext;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private HandlerContext handlerContext;

    private AssetLoader assetLoader;

    private ManifestLoader manifestLoader;

    private ApplicationNodeMetadataContext applicationNodeMetadataContext;

    private Thread hook = new Thread(this::shutdown);

    private static final Logger logger = LoggerFactory.getLogger(SimpleContext.class);

    @Override
    public void start() {
        getRuntime().addShutdownHook(hook);
        getResourceContext().start();
        getSchedulerContext().start();
        getIndexContext().start();
        getHandlerContext().start();
        getApplicationNodeMetadataContext().start();
        getManifestLoader().loadAndRunIfNecessary();
        runStartupManifest();
    }

    private void runStartupManifest() {

        final StartupManifest startupManifest = getManifestLoader().getStartupManifest();

        if (startupManifest == null) {
            logger.info("No startup Resources to run.  Skipping.");
            return;
        } else if (startupManifest.getModulesByName() == null) {
            logger.info("No startup modules specified in manifest.  Skipping.");
            return;
        }

        for (final Map.Entry<String, StartupModule> entry : startupManifest.getModulesByName().entrySet()) {

            final StartupModule startupModule = entry.getValue();

            if (startupModule == null) {
                logger.info("Startup module '{}' specifies no operation.  Skipping.", entry.getKey());
                continue;
            }

            final String module = entry.getKey();
            final Map<String, StartupOperation> startupOperationsByName = startupModule.getOperationsByName();

            for (final StartupOperation startupOperation : startupOperationsByName.values()) {

                final String method = startupOperation.getMethod();
                logger.info("Executing startup operation {}: {}.{}", startupOperation.getName(), module, method);

                final Consumer<Throwable> failure = ex -> {
                    logger.error("Startup exception caught for module: {}, method: {}.", module, method, ex);
                };

                final Consumer<Object> success = result -> {
                    logger.info("Startup operation '{}: {}.{}': Success.", startupOperation.getName(), module, method);
                };

                // unused for now
                final SimpleAttributes attributes = new SimpleAttributes();
                getHandlerContext().invokeRetainedHandlerAsync(success, failure, attributes, module, method);

            }
        }

    }

    @Override
    public void shutdown() {

        // Remove the shutdown hook.
        if (Thread.currentThread() != hook) getRuntime().removeShutdownHook(hook);

        // Stops all contexts first
        getHandlerContext().stop();
        getIndexContext().stop();
        getSchedulerContext().stop();
        getResourceContext().stop();
        getApplicationNodeMetadataContext().stop();

        // Then stops all services
        getResourceLoader().close();
        getAssetLoader().close();
        getManifestLoader().close();

    }

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Inject
    public void setResourceContext(ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    @Override
    public SchedulerContext getSchedulerContext() {
        return schedulerContext;
    }

    @Override
    public IndexContext getIndexContext() {
        return indexContext;
    }

    @Override
    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    @Inject
    public void setSchedulerContext(SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Inject
    public void setAssetLoader(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    @Inject
    public void setIndexContext(IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Inject
    public void setHandlerContext(HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    public ManifestLoader getManifestLoader() {
        return manifestLoader;
    }

    @Inject
    public void setManifestLoader(ManifestLoader manifestLoader) {
        this.manifestLoader = manifestLoader;
    }

    public ApplicationNodeMetadataContext getApplicationNodeMetadataContext() {
        return applicationNodeMetadataContext;
    }

    @Inject
    public void setApplicationNodeMetadataContext(ApplicationNodeMetadataContext applicationNodeMetadataContext) {
        this.applicationNodeMetadataContext = applicationNodeMetadataContext;
    }
}
