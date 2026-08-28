package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.address.RemoteElementMethodAddress;
import dev.getelements.elements.sdk.cluster.id.HasInstanceId;
import dev.getelements.elements.sdk.cluster.remote.annotation.RoutingAddressSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link AddressingStrategy} which relies on {@link HasInstanceId} to determine the instance id. Failures to address
 * are logged as warnings.
 */
public class HasInstanceIdAddressingStrategy implements AddressingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HasInstanceIdAddressingStrategy.class);

    @Override
    public RemoteElementMethodAddress resolve(final RemoteElementMethodAddress input, final Object[] arguments) {

        if (arguments.length != 1) {

            logger.warn("Method {} may specify only one {}",
                    input.path(),
                    RoutingAddressSource.class.getSimpleName()
            );

            return input;

        }

        if (arguments[0] instanceof HasInstanceId hasInstanceId) {
            return hasInstanceId
                    .findInstanceId()
                    .map(input::withInstanceId)
                    .orElse(input);
        } else {

            logger.warn("Method {} parameter must be of type {}",
                    input.path(),
                    HasInstanceId.class.getSimpleName()
            );

            return input;
        }

    }

}
