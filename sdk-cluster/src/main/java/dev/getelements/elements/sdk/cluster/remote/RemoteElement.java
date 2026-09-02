package dev.getelements.elements.sdk.cluster.remote;

import dev.getelements.elements.sdk.cluster.remote.proxy.ProxyBuilder;
import dev.getelements.elements.sdk.cluster.remote.record.RemoteElementRecord;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementServiceKey;

/**
 * Represents a remote element.
 */
public interface RemoteElement {

    /**
     * Gets the {@link ElementDefinitionRecord} which provides
     *
     * @return the name of the element.
     */
    RemoteElementRecord getElementRecord();

    /**
     * Begins building a proxy for a service inside this {@link RemoteElement}.
     *
     * @param proxy the proxy type
     * @return return the {@link ProxyBuilder} for the remote service
     * @param <ProxyT> the proxy type
     */
    default <ProxyT> ProxyBuilder<ProxyT> getProxy(final Class<ProxyT> proxy) {
        return getProxy(proxy, "");
    }

    /**
     * Begins building a proxy for a service inside this {@link RemoteElement}.
     *
     * @param proxy the proxy type
     * @param name the proxy name
     * @return return the {@link ProxyBuilder} for the remote service
     * @param <ProxyT> the proxy type
     */
    default <ProxyT> ProxyBuilder<ProxyT> getProxy(final Class<ProxyT> proxy, String name) {
        return getProxy(new ElementServiceKey<>(proxy, name));
    }

    /**
     * Begins building a proxy for a service inside this {@link RemoteElement}.
     *
     * @param key the key
     * @return return the {@link ProxyBuilder} for the remote service
     * @param <ProxyT> the proxy type
     */
    <ProxyT> ProxyBuilder<ProxyT> getProxy(ElementServiceKey<ProxyT> key);

}
