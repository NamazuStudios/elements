package dev.getelements.elements.rt;

import dev.getelements.elements.rt.manifest.http.HttpManifest;
import dev.getelements.elements.rt.manifest.model.ModelManifest;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;
import dev.getelements.elements.rt.manifest.startup.StartupManifest;
import dev.getelements.elements.rt.manifest.startup.StartupModule;
import dev.getelements.elements.rt.manifest.startup.StartupOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static dev.getelements.elements.rt.Context.LOCAL;

public class SimpleManifestContext implements ManifestContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleManifestContext.class);

    private HandlerContext handlerContext;

    private Provider<ManifestLoader> manifestLoaderProvider;

    private final AtomicReference<ManifestLoader> loaderAtomicReference = new AtomicReference<>();

    @Override
    public void start() {

        final var loader = getManifestLoaderProvider().get();

        try {
            if (!loaderAtomicReference.compareAndSet(null, loader)) {
                throw new IllegalStateException("Already running!");
            }

            runStartupManifest();

        } catch (Exception ex) {
            logger.error("Caught exception starting up.", ex);
            loader.close();
        }

    }

    @Override
    public void stop() {
        final var loader = loaderAtomicReference.getAndSet(null);
        if (loader == null) throw new IllegalStateException("Not running.");
        loader.close();
    }

    @Override
    public ModelManifest getModelManifest() {
        return getCurrentLoader().getModelManifest();
    }

    @Override
    public HttpManifest getHttpManifest() {
        return getCurrentLoader().getHttpManifest();
    }

    @Override
    public SecurityManifest getSecurityManifest() {
        return getCurrentLoader().getSecurityManifest();
    }

    @Override
    public StartupManifest getStartupManifest() {
        return getCurrentLoader().getStartupManifest();
    }

    private final ManifestLoader getCurrentLoader() {
        final var loader = loaderAtomicReference.get();
        if (loader == null) throw new IllegalStateException("not running.");
        return loader;
    }

    public Provider<ManifestLoader> getManifestLoaderProvider() {
        return manifestLoaderProvider;
    }

    @Inject
    public void setManifestLoaderProvider(Provider<ManifestLoader> manifestLoaderProvider) {
        this.manifestLoaderProvider = manifestLoaderProvider;
    }

    public HandlerContext getHandlerContext() {
        return handlerContext;
    }

    @Inject
    public void setHandlerContext(@Named(LOCAL) HandlerContext handlerContext) {
        this.handlerContext = handlerContext;
    }

    private void runStartupManifest() {

        final StartupManifest startupManifest = getStartupManifest();

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

}
