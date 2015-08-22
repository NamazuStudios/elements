package com.namazustudios.socialengine.rt;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Created by patricktwohig on 8/22/15.
 */
public class CGLibLockFactory<ResourceT extends  Resource> implements ResourceLockFactory<ResourceT> {

    private static final Logger LOG = LoggerFactory.getLogger(CGLibLockFactory.class);

    private static final MethodInterceptor LOCK_METHOD_INTERCEPTOR = new MethodInterceptor() {

        final Class<?>[] hashCodeParameterTypes = new Class<?>[]{};
        final Class<?>[] equalsParameterTypes = new Class<?>[]{ Object.class };

        @Override
        public Object intercept(final Object object,
                                final Method method,
                                final Object[] arguments,
                                final MethodProxy methodProxy) throws Throwable {

            if ("equals".equals(method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), equalsParameterTypes)) {
                return System.identityHashCode(object);
            } else if ("hashCode".equals(method.getName()) &&
                    Arrays.equals(method.getParameterTypes(), hashCodeParameterTypes)) {
                return this == object;
            } else {
                LOG.error("Method {} called on lock object: {}", method, object);
                return methodProxy.invokeSuper(object, arguments);
            }

        }

    };

    final Enhancer lockEnhancer;

    private final Class<ResourceT> resourceTClass;

    public CGLibLockFactory(final Class<ResourceT> resourceTClass) {

        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(resourceTClass);
        enhancer.setCallback(LOCK_METHOD_INTERCEPTOR);

        this.lockEnhancer = enhancer;
        this.resourceTClass = resourceTClass;

    }

    @Override
    public ResourceT createLock() {
        return resourceTClass.cast(lockEnhancer.create());
    }

    @Override
    public boolean isLock(final ResourceT resource) {
        return lockEnhancer.isEnhanced(resource.getClass());
    }

}
