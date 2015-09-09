package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.edge.EdgeResource;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

/**
 * Created by patricktwohig on 9/9/15.
 */
public class ProxyLockFactory<ResourceT extends Resource> implements ResourceLockFactory<ResourceT> {

    private static final Logger LOG = LoggerFactory.getLogger(ProxyLockFactory.class);

    private final Class<ResourceT> resourceTClass;

    public ProxyLockFactory(final Class<ResourceT> resourceTClass) {

        if (!resourceTClass.isInterface()) {
            throw new IllegalArgumentException(resourceTClass + " is not an interface type.");
        }

        this.resourceTClass = resourceTClass;

    }

    @Override
    @SuppressWarnings("unchecked")
    public ResourceT createLock() {
        return (ResourceT) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?> [] {resourceTClass},
            new InvocationHandler() {

                final Class<?>[] hashCodeParameterTypes = new Class<?>[]{};

                final Class<?>[] closeParameterTypes = new Class<?>[]{};

                final Class<?>[] equalsParameterTypes = new Class<?>[]{ Object.class };

                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    if ("hashCode".equals(method.getName()) &&
                        Arrays.equals(method.getParameterTypes(), equalsParameterTypes)) {
                        return System.identityHashCode(proxy);
                    } else if ("equals".equals(method.getName()) &&
                               Arrays.equals(method.getParameterTypes(), hashCodeParameterTypes)) {
                        return proxy == args[0];
                    } else if ("close".equals(method.getName()) &&
                               Arrays.equals(method.getParameterTypes(), closeParameterTypes)) {
                        // Close does nothing, it should be safe to call.
                        return null;
                    } else {
                        LOG.error("Method {} called on lock object: {}", method, proxy);
                        return null;
                    }
                }
            }
        );
    }

    @Override
    public boolean isLock(ResourceT resource) {
        return Proxy.isProxyClass(resource.getClass());
    }

    private static final ProxyLockFactory<EdgeResource> EDGE_RESOURCE_PROXY_LOCK_FACTORY =
        new ProxyLockFactory<>(EdgeResource.class);

    public static ProxyLockFactory<EdgeResource> edgeResourceProxyLockFactory() {
        return EDGE_RESOURCE_PROXY_LOCK_FACTORY;
    }

    private static final ProxyLockFactory<InternalResource> INTERNAL_RESOURCE_PROXY_LOCK_FACTORY =
        new ProxyLockFactory<>(InternalResource.class);

    public static ProxyLockFactory<InternalResource> internalResourceProxyLockFactory() {
        return INTERNAL_RESOURCE_PROXY_LOCK_FACTORY;
    }

}

