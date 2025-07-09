package dev.getelements.elements.sdk.local.internal;

import dev.getelements.elements.jetty.ElementsWebServices;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

public class StandardElementsLocal implements ElementsLocal {

    private final Lock lock = new ReentrantLock();

    private MutableElementRegistry rootElementRegistry;

    private ElementsWebServices elementsWebServices;

    @Override
    public StandardElementsLocal start() {

        try (var mon = Monitor.enter(lock)){
            getElementsWebServices().start();
        }

        return this;

    }

    @Override
    public StandardElementsLocal run() {

        final ElementsWebServices elementsWebServices;

        try (var mon = Monitor.enter(lock)){
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
                setElementsWebServices(null);
            }
        }
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
