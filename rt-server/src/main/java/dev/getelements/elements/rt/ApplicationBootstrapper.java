package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.ApplicationId;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Responsible for loading the code to a fresh application, ensuring the minimum to run an application within the
 * cluster.
 */
public interface ApplicationBootstrapper {

    /**
     * Bootstraps the supplied {@link dev.getelements.elements.rt.id.ApplicationId}. This will deploy the skeleton
     * code for the associated application.
     *
     * Calling this must ensure that the subsequent calls to {@link ManifestContext} will return non-null manifest
     * instances which can be use to drive the application.
     *
     * @param bootstrapUserMetadata the {@link BootstrapUserMetadata} of the principal performing the action.
     * @param applicationId the {@link ApplicationId} to bootstrap.
     */
    void bootstrap(BootstrapUserMetadata bootstrapUserMetadata, ApplicationId applicationId);

    /**
     * Uesd to indicate who is performing the bootstrapping action.
     */
    interface BootstrapUserMetadata {

        /**
         * The username of the principal performing the operation.
         *
         * @return the username of the principal performing the operation.
         */
        String getName();

        /**
         * The email address of the principal performing the operation.
         *
         * @return the email of the principal performing the operation.
         */
        String getEmail();

    }

    /**
     * Created by patricktwohig on 8/22/17.
     */
    interface BootstrapResources {

        /**
         * Gets a {@link Map <Path,  Supplier < InputStream >>} instance which will provide
         * a comprehensive set of resources which should go into a freshly created set
         * of code.
         *
         * @return a {@link Map<Path, Supplier<InputStream>>} instance
         */
        Map<Path, Supplier<InputStream>> getBootstrapResources();

    }

}
