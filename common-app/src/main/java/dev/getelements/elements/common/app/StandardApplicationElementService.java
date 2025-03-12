package dev.getelements.elements.common.app;

import dev.getelements.elements.rt.ApplicationAssetLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementPathLoader.newDefaultInstance;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.forUniqueName;

public class StandardApplicationElementService implements ApplicationElementService {

    private ElementRegistry rootElementRegistry;

    private ApplicationAssetLoader applicationAssetLoader;

    private final ConcurrentMap<ApplicationId, Lock> locks = new ConcurrentHashMap<>();

    private final ConcurrentMap<ApplicationId, ApplicationElementRecord> records = new ConcurrentHashMap<>();

    private Monitor lock(ApplicationId applicationId) {
        final var lock = locks.computeIfAbsent(applicationId, id -> new ReentrantLock());
        return Monitor.enter(lock);
    }

    @Override
    public ApplicationElementRecord getOrLoadApplication(final Application application) {

        final var applicationId = forUniqueName(application.getId());

        final var lazyValue = new SimpleLazyValue<>(() -> {
            final var path = getApplicationAssetLoader().getAssetPath(applicationId);
            final var registry = getRootElementRegistry().newSubordinateRegistry();
            final var loader = newDefaultInstance();
            final var elements = loader.load(registry, path).toList();
            return new ApplicationElementRecord(applicationId, registry, elements);
        });

        try (final var monitor = lock(applicationId)) {
            return records.computeIfAbsent(applicationId, aid -> lazyValue.get());
        }

    }

    public ElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) ElementRegistry rootElementRegistry) {
        this.rootElementRegistry = rootElementRegistry;
    }

    public ApplicationAssetLoader getApplicationAssetLoader() {
        return applicationAssetLoader;
    }

    @Inject
    public void setApplicationAssetLoader(@Named(ApplicationAssetLoader.ELEMENT_STORAGE) ApplicationAssetLoader applicationAssetLoader) {
        this.applicationAssetLoader = applicationAssetLoader;
    }

}
