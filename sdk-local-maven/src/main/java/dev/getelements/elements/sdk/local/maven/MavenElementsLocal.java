package dev.getelements.elements.sdk.local.maven;

import dev.getelements.elements.jetty.ElementsWebServices;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.local.maven.Maven.mvn;

public class MavenElementsLocal implements ElementsLocal {

    public static final String DEPLOYMENTS = "dev.getelements.elements.sdk.local.maven.deployments";

    public static final String SOURCE_DIRECTORIES = "dev.getelements.elements.sdk.local.maven.source.directories";

    private final Lock lock = new ReentrantLock();

    private Set<Path> sourceDirectories;

    private Set<TransientDeploymentRequest> deployments;

    private ElementsWebServices elementsWebServices;

    private ElementRuntimeService elementRuntimeService;

    private MutableElementRegistry rootElementRegistry;

    @Override
    public MavenElementsLocal start() {

        getSourceDirectories().forEach(path -> mvn(path, "-DskipTests", "install"));

        try (var mon = Monitor.enter(lock)) {
            getElementsWebServices().start();
        }

        getDeployments().forEach(getRuntimeService()::loadTransientDeployment);

        return this;

    }

    @Override
    public MavenElementsLocal run() {

        final ElementsWebServices elementsWebServices;

        try (var mon = Monitor.enter(lock)) {
            elementsWebServices = getElementsWebServices();
        }

        elementsWebServices.run();
        return this;

    }

    @Override
    public void close() {
        try (var mon = Monitor.enter(lock)){
            if (getElementsWebServices() != null) {
                getElementsWebServices().stop();
            }
        }
    }

    public Set<TransientDeploymentRequest> getDeployments() {
        return deployments;
    }

    @Inject
    public void setDeployments(@Named(DEPLOYMENTS) Set<TransientDeploymentRequest> deployments) {
        this.deployments = deployments;
    }

    public Set<Path> getSourceDirectories() {
        return sourceDirectories;
    }

    @Inject
    public void setSourceDirectories(@Named(SOURCE_DIRECTORIES) Set<Path> sourceDirectories) {
        this.sourceDirectories = sourceDirectories;
    }

    @Override
    public ElementRuntimeService getRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

    @Override
    public MutableElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) final MutableElementRegistry rootElementRegistry) {
        this.rootElementRegistry = rootElementRegistry;
    }

    public ElementsWebServices getElementsWebServices() {
        return elementsWebServices;
    }

    @Inject
    public void setElementsWebServices(ElementsWebServices elementsWebServices) {
        this.elementsWebServices = elementsWebServices;
    }

}
