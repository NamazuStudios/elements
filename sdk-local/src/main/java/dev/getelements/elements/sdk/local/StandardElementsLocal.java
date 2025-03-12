package dev.getelements.elements.sdk.local;

import dev.getelements.elements.jetty.ElementsWebServices;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

class StandardElementsLocal implements ElementsLocal {

    private final Lock lock = new ReentrantLock();

    private ElementRegistry rootElementRegistry;

    private ElementsWebServices elementsWebServices;

    @Override
    public void start() {
        try (var mon = Monitor.enter(lock)){
            getElementsWebServices().start();
        } finally {
            elementsWebServices = null;
        }
    }

    @Override
    public void run() {
        final ElementsWebServices elementsWebServices;

        try (var mon = Monitor.enter(lock)){
            elementsWebServices = getElementsWebServices();
        }

        elementsWebServices.run();

    }

    @Override
    public void close() {
        try (var mon = Monitor.enter(lock)){
            if (getElementsWebServices() != null) {
                getElementsWebServices().stop();
            }
        } finally {
            elementsWebServices = null;
        }
    }

    @Override
    public ElementRegistry getRootElementRegistry() {
        return rootElementRegistry;
    }

    @Inject
    public void setRootElementRegistry(@Named(ROOT) final ElementRegistry rootElementRegistry) {
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
