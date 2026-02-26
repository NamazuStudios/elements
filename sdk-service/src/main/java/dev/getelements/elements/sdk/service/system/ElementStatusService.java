package dev.getelements.elements.sdk.service.system;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.system.*;

import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Manages the deployments of applications.
 *
 * Created by patricktwohig on 7/13/15.
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ElementStatusService {

    /**
     * Gets all builtin SPIs.
     *
     * @return all builtin SPIs
     */
    List<ElementSpi> getAllBuiltinSpis();

    /**
     * Gets all system elements available to be deployed.
     *
     * @return the metadata for all system elements
     */
    List<ElementMetadata> getAllSystemElements();

    /**
     * Gets all deployments for all runtimes.
     *
     * @return the list of runtimes.
     */
    List<ElementRuntimeStatus> getAllRuntimes();

    /**
     * Gets all deployments for all containers.
     *
     * @return the list of all containers.
     */
    List<ElementContainerStatus> getAllContainers();

    /**
     * Lists al {@link ElementFeature}s that can be used by an Element.
     * @return the features
     */
    List<ElementFeature> getAllFeatures();

}
