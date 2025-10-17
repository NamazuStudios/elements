package dev.getelements.elements.service.application;

import dev.getelements.elements.common.app.ApplicationDeploymentService;
import dev.getelements.elements.common.app.ApplicationDeploymentService.DeploymentRecord;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.model.application.ApplicationStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.record.ElementMetadata;
import dev.getelements.elements.sdk.service.application.ApplicationStatusService;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_SERVE;

public class SuperUserApplicationStatusService implements ApplicationStatusService {

    private MapperRegistry mapperRegistry;

    private ElementRegistry elementRegistry;

    private ApplicationDeploymentService deploymentService;

    @Override
    public List<ElementMetadata> getAllSystemElements() {
        return getElementRegistry().stream().map(ElementMetadata::from).toList();
    }

    @Override
    public List<ApplicationStatus> getAllDeployments() {

        final var mapper = getMapperRegistry().getMapper(DeploymentRecord.class, ApplicationStatus.class);

        return getDeploymentService()
                .listAllDeployments()
                .stream()
                .map(mapper::forward)
                .toList();

    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

    public ApplicationDeploymentService getDeploymentService() {
        return deploymentService;
    }

    @Inject
    public void setDeploymentService(@Named(APP_SERVE) ApplicationDeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

}
