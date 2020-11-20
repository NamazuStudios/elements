package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.manifest.startup.StartupManifest;
import com.namazustudios.socialengine.rt.manifest.startup.StartupModule;
import com.namazustudios.socialengine.rt.manifest.startup.StartupOperation;
import com.namazustudios.socialengine.rt.remote.Node;
import com.namazustudios.socialengine.rt.remote.NodeLifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.function.Consumer;

import static java.lang.Runtime.getRuntime;

@Singleton
public class SimpleContext implements Context, NodeLifecycle {

    private Scheduler scheduler;

    private SchedulerContext schedulerContext;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private ResourceContext resourceContext;

    private IndexContext indexContext;

    private HandlerContext handlerContext;

    private TaskContext taskContext;

    private AssetLoader assetLoader;

    private ManifestLoader manifestLoader;

    private Thread hook = new Thread(this::shutdown);

    private static final Logger logger = LoggerFactory.getLogger(SimpleContext.class);

    @Override
    public void start() {
        getRuntime().addShutdownHook(hook);
        getTaskContext().start();
        getResourceContext().start();
        getSchedulerContext().start();
        getIndexContext().start();
        getHandlerContext().start();
        getManifestLoader().loadAndRunIfNecessary();
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
        getTaskContext().stop();

        // Then stops all services
        getResourceLoader().close();
        getAssetLoader().close();
        getManifestLoader().close();

    }

    @Override
    public void nodePreStart(final Node node) {
        start();
    }

    @Override
    public void nodePostStart(final Node node) {
        runStartupManifest();
    }

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
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

    @Override
    public TaskContext getTaskContext() {
        return taskContext;
    }

    @Inject
    public void setResourceContext(@Named(LOCAL) ResourceContext resourceContext) {
        this.resourceContext = resourceContext;
    }

    @Inject
    public void setSchedulerContext(@Named(LOCAL) SchedulerContext schedulerContext) {
        this.schedulerContext = schedulerContext;
    }

    @Inject
    public void setIndexContext(@Named(LOCAL) IndexContext indexContext) {
        this.indexContext = indexContext;
    }

    @Inject
    public void setHandlerContext(@Named(LOCAL) HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    @Inject
    public void setTaskContext(@Named(LOCAL) TaskContext taskContext) {
        this.taskContext = taskContext;
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

    public ManifestLoader getManifestLoader() {
        return manifestLoader;
    }

    @Inject
    public void setManifestLoader(ManifestLoader manifestLoader) {
        this.manifestLoader = manifestLoader;
    }

}
