package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ServiceLocator;
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

        return new SharedElement(getElementRecord(), getServiceLocator(), parent);

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
