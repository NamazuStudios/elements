package dev.getelements.elements.sdk.local;

import com.restfb.types.Url;
import dev.getelements.elements.common.app.ApplicationElementService;
import dev.getelements.elements.common.app.StandardApplicationElementService;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementLoaderFactory;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.reflection.ElementReflectionUtils;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.forUniqueName;

class LocalApplicationElementService implements ApplicationElementService {

    private final Lock lock = new ReentrantLock();

    private List<LocalApplicationElementRecord> localElements;

    private ElementRegistry rootElementRegistry;

    private StandardApplicationElementService standardApplicationElementService;

    private final Map<ApplicationId, ApplicationElementRecord> records = new HashMap<>();

    @Override
    public ElementRegistry getElementRegistry(final Application application) {
        return getStandardApplicationElementService().getElementRegistry(application);
    }

    @Override
    public ApplicationElementRecord getOrLoadApplication(final Application application) {

        final var registry = getElementRegistry(application);
        final var applicationId = forUniqueName(application.getId());

        try (var mon = Monitor.enter(lock)) {

            final var local = records.computeIfAbsent(applicationId, aid -> {

                final var locals = localElements.stream()
                        .filter(lar -> lar.matches(application))
                        .map(this::doLoadElement)
                        .map(registry::register)
                        .toList();

                return new ApplicationElementRecord(applicationId, registry, locals);

            });

            final var standard = standardApplicationElementService.getOrLoadApplication(application);

            final var combined = new ArrayList<Element>();
            combined.addAll(local.elements());
            combined.addAll(standard.elements());

            return new ApplicationElementRecord(applicationId, registry, combined);

        }

    }

    private ElementLoader doLoadElement(final LocalApplicationElementRecord lar) {
        return ElementLoaderFactory
                .getDefault()
                .getIsolatedLoader(
                        lar.attributes(),
                        getClass().getClassLoader(),
                        cl -> new SecureClassLoader(cl){},
                        edr -> edr.pkgName().equals(lar.packageName())
                );
    }

    public List<LocalApplicationElementRecord> getLocalElements() {
        return localElements;
    }

    @Inject
    public void setLocalElements(List<LocalApplicationElementRecord> localElements) {
        this.localElements = localElements;
    }

    public ElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) ElementRegistry rootElementRegistry) {
        this.rootElementRegistry = rootElementRegistry;
    }

    public StandardApplicationElementService getStandardApplicationElementService() {
        return standardApplicationElementService;
    }

    @Inject
    public void setStandardApplicationElementService(StandardApplicationElementService standardApplicationElementService) {
        this.standardApplicationElementService = standardApplicationElementService;
    }

}
