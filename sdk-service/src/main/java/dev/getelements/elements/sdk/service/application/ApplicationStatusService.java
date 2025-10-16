package dev.getelements.elements.sdk.service.application;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.application.ApplicationStatus;
import dev.getelements.elements.sdk.record.ElementMetadata;

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
public interface ApplicationStatusService {

    /**
     * Gets all system elements available to be deployed.
     *
     * @return the metadata for all system elements
     */
    List<ElementMetadata> getAllSystemElements();

    /**
     * Gets all deployments for all applications.
     *
     * @return all deployments
     */
    List<ApplicationStatus> getAllDeployments();

}
