package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.InstanceDiscoveryService;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.namazustudios.socialengine.rt.Constants.INSTANCE_DISCOVERY_SERVICE;

/**
 * Gets an instance of {@link InstanceDiscoveryService} by reading system defines and if none are found reverting to a
 * default implementation.
 */
public class InstanceDiscoveryServiceFactory {

    private static final Logger logger = LoggerFactory.getLogger(InstanceDiscoveryServiceFactory.class);

    private static final Class<StaticInstanceDiscoveryService> DEFAULT_TYPE = StaticInstanceDiscoveryService.class;

    private static InstanceDiscoveryServiceFactory instance = new InstanceDiscoveryServiceFactory();

    /**
     * Gets the shared instance of the {@link InstanceDiscoveryServiceFactory}.
     *
     * @return the shared instance
     */
    public static InstanceDiscoveryServiceFactory getInstance() {
        return instance;
    }

    private static Class<? extends InstanceDiscoveryService> determineType() {

        final Class<?> cls;
        final String className = System.getProperty(INSTANCE_DISCOVERY_SERVICE);

        try {

            logger.info("Attemting to use InstanceDiscoveryService {}", className);
            cls = Class.forName(className);

            if (InstanceDiscoveryService.class.isAssignableFrom(cls)) {
                return (Class<? extends InstanceDiscoveryService>) cls;
            } else {
                logger.warn("Type {} is not a subclass of InstanceDiscoveryService. Using {}", className, DEFAULT_TYPE.getName());
                return (Class<? extends InstanceDiscoveryService>) DEFAULT_TYPE;
            }

        } catch (ClassNotFoundException ex) {
            logger.warn("Requested InstanceDiscoveryService type not found on classpath. Using {}", DEFAULT_TYPE.getName(), ex);
            return (Class<? extends InstanceDiscoveryService>) DEFAULT_TYPE;
        }

    }

    private final Class<? extends InstanceDiscoveryService> instanceDiscoveryServiceType;

    private InstanceDiscoveryServiceFactory() {
        instanceDiscoveryServiceType = determineType();
    }

    /**
     * Gets the {@link Class} of the {@link InstanceDiscoveryService} to use.
     *
     * @return the {@link Class} to use.
     */
    public Class<? extends InstanceDiscoveryService> getInstanceDiscoveryServiceType() {
        return instanceDiscoveryServiceType;
    }

    /**
     * Returns a new instance of {@link InstanceDiscoveryService}.
     *
     * @return the instance of {@link InstanceDiscoveryService}
     */
    public InstanceDiscoveryService newInstance() {
        try {
            return instanceDiscoveryServiceType.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new InternalException(e);
        }
    }

}
