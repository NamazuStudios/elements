package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.*;
import dev.getelements.elements.sdk.record.ElementRecord;

public class DefaultSharedElementLoader implements ElementLoader {

    private ElementRecord elementRecord;

    private ServiceLocator serviceLocator;

    @Override
    public Element load(final ElementRegistry parent) {

        if (getElementRecord() == null) {
            throw new IllegalStateException("No ElementRecord set.");
        }

        if (getServiceLocator() == null) {
            throw new IllegalStateException("No ServiceLocator set.");
        }

        final var element = new  SharedElement(getElementRecord(), getServiceLocator(), parent);

        final var event = Event.builder()
                .named(SYSTEM_EVENT_ELEMENT_LOADED)
                .build();

        element.publish(event);

        return element;

    }

    @Override
    public ElementRecord getElementRecord() {
        return elementRecord;
    }

    public void setElementRecord(ElementRecord elementRecord) {
        this.elementRecord = elementRecord;
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

}
