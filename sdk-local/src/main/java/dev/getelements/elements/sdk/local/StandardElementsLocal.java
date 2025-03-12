package dev.getelements.elements.sdk.local;

import dev.getelements.elements.jetty.ElementsWebServices;
import dev.getelements.elements.sdk.util.Monitor;
import jakarta.inject.Inject;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class StandardElementsLocal implements ElementsLocal {

    private final Lock lock = new ReentrantLock();

    private ElementsWebServices elementsWebServices;

    @Override
    public void start() {
        try (var mon = Monitor.enter(lock)){
            elementsWebServices.start();
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
            elementsWebServices.stop();
        } finally {
            elementsWebServices = null;
        }
    }

    public ElementsWebServices getElementsWebServices() {
        return elementsWebServices;
    }

    @Inject
    public void setElementsWebServices(ElementsWebServices elementsWebServices) {
        this.elementsWebServices = elementsWebServices;
    }

}
