package dev.getelements.elements.rt.remote;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ServiceLocator;
import jakarta.inject.Inject;

/**
 * @deprecated This needs to be replaced with a dispatch to an {@link Element} via {@link ElementRegistry}
 */
@Deprecated
public class ServiceLocatorLocalInvocationDispatcher extends AbstractLocalInvocationDispatcher {

    private ServiceLocator serviceLocator;

    @Override
    protected Object resolve(final Class<?> type) {
        return getServiceLocator().getInstance(type);
    }

    @Override
    protected Object resolve(final Class<?> type, final String name) {
        return getServiceLocator().getInstance(type, name);
    }

    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    @Inject
    public void setServiceLocator(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

}
