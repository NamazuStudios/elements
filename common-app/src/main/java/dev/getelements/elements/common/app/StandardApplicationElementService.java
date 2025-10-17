package dev.getelements.elements.common.app;

import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.cluster.ApplicationAssetLoader;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementPathLoader.newDefaultInstance;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.forUniqueName;

public class StandardApplicationElementService implements ApplicationElementService {

    private MutableElementRegistry rootElementRegistry;

    private ApplicationAssetLoader applicationAssetLoader;

    private final Lock lock = new ReentrantLock();

    private final Map<ApplicationId, MutableElementRegistry> registries = new HashMap<>();

    private final Map<ApplicationId, ApplicationElementRecord> records = new HashMap<>();

    @Override
    public MutableElementRegistry getElementRegistry(final Application application) {
        try (var mon = Monitor.enter(lock)) {
            final var applicationId = forUniqueName(application.getId());
            return doGetOrLoadElementRegistry(applicationId);
        }
    }

    private MutableElementRegistry doGetOrLoadElementRegistry(final ApplicationId applicationId) {
        return registries.computeIfAbsent(
                applicationId,
                id -> rootElementRegistry.newSubordinateRegistry()
        );
    }

    @Override
    public ApplicationElementRecord getOrLoadApplication(final Application application) {

        final var applicationId = forUniqueName(application.getId());

        try (final var monitor = Monitor.enter(lock)) {
            return records.computeIfAbsent(applicationId, aid -> {
                final var registry = doGetOrLoadElementRegistry(applicationId);
                final var loader = newDefaultInstance();
                final var path = getApplicationAssetLoader().getAssetPath(applicationId);
                final var elements = loader.load(registry, path).toList();
                return new ApplicationElementRecord(applicationId, registry, elements);
            });
        }

    }

    public MutableElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) MutableElementRegistry rootElementRegistry) {
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
