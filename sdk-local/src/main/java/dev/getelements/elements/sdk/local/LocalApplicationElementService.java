package dev.getelements.elements.sdk.local;

import dev.getelements.elements.common.app.ApplicationElementService;
import dev.getelements.elements.common.app.StandardApplicationElementService;
import dev.getelements.elements.rt.exception.ApplicationCodeNotFoundException;
import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementLoaderFactory;
import dev.getelements.elements.sdk.ElementLoaderFactory.ClassLoaderConstructor;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.cluster.id.ApplicationId;
import dev.getelements.elements.sdk.exception.SdkElementNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.Monitor;
import dev.getelements.elements.sdk.util.reflection.ElementReflectionUtils;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.cluster.id.ApplicationId.forUniqueName;
import static java.lang.ClassLoader.getSystemClassLoader;

class LocalApplicationElementService implements ApplicationElementService {

    private static final Logger logger = LoggerFactory.getLogger(LocalApplicationElementService.class);

    private final Lock lock = new ReentrantLock();

    private List<LocalApplicationElementRecord> localElements;

    private ElementRegistry rootElementRegistry;

    private ClassLoaderConstructor classLoaderConstructor;

    private StandardApplicationElementService standardApplicationElementService;

    private final Map<ApplicationId, ApplicationElementRecord> records = new HashMap<>();

    @Override
    public MutableElementRegistry getElementRegistry(final Application application) {
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

            final var combined = new ArrayList<>(local.elements());

            try {
                final var standard = standardApplicationElementService.getOrLoadApplication(application);
                combined.addAll(standard.elements());
            } catch (ApplicationCodeNotFoundException ex) {
                logger.trace("Standard application code not found. Skipping {}", applicationId, ex);
            }

            return new ApplicationElementRecord(applicationId, registry, combined);

        }

    }

    private ElementLoader doLoadElement(final LocalApplicationElementRecord lar) {

        final var preloadClassLoader = new DelegatingPreloadClassLoader();

        final var elf = ServiceLoader
                .load(ElementLoaderFactory.class, preloadClassLoader)
                .findFirst().orElseThrow(() -> new SdkException(
                        "No SPI (Service Provider Implementation) for " +
                        ElementLoaderFactory.class.getName())
                );

        final var elementReflectionUtils = ElementReflectionUtils.getInstance();

        final var elementDefinitionRecord = elf
            .findElementDefinitionRecord(
                preloadClassLoader,
                lar.attributes(),
                edr -> edr.name().equals(lar.elementName())
            )
            .orElseThrow(SdkElementNotFoundException::new);

        return elf.getIsolatedLoader(
                        lar.attributes(),
                        preloadClassLoader,
                        parentClassLoader -> {
                            final var elementClassLoader = getClassLoaderConstructor().apply(parentClassLoader);
                            elementReflectionUtils.injectBeanProperties(elementClassLoader, elementDefinitionRecord);
                            return elementClassLoader;
                        },
                        edr -> edr.name().equals(lar.elementName())
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

    public ClassLoaderConstructor getClassLoaderConstructor() {
        return classLoaderConstructor;
    }

    @Inject
    public void setClassLoaderConstructor(ClassLoaderConstructor classLoaderConstructor) {
        this.classLoaderConstructor = classLoaderConstructor;
    }

    public StandardApplicationElementService getStandardApplicationElementService() {
        return standardApplicationElementService;
    }

    @Inject
    public void setStandardApplicationElementService(StandardApplicationElementService standardApplicationElementService) {
        this.standardApplicationElementService = standardApplicationElementService;
    }

}
