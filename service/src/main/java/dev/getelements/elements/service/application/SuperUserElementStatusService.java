package dev.getelements.elements.service.application;

import dev.getelements.elements.common.app.ElementContainerService;
import dev.getelements.elements.common.app.ElementContainerService.ContainerRecord;
import dev.getelements.elements.common.app.ElementRuntimeService;
import dev.getelements.elements.common.app.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.model.system.ElementContainerStatus;
import dev.getelements.elements.sdk.model.system.ElementMetadata;
import dev.getelements.elements.sdk.model.system.ElementRuntimeStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.system.ElementStatusService;
import jakarta.inject.Inject;

import java.util.List;

public class SuperUserElementStatusService implements ElementStatusService {

    private MapperRegistry mapperRegistry;

    private ElementRegistry elementRegistry;

    private ElementRuntimeService elementRuntimeService;

    private ElementContainerService elementContainerService;

    @Override
    public List<ElementMetadata> getAllSystemElements() {
        return getElementRegistry().stream().map(ElementMetadata::from).toList();
    }

    @Override
    public List<ElementRuntimeStatus> getAllRuntimes() {

        final var mapper = getMapperRegistry().getMapper(RuntimeRecord.class, ElementRuntimeStatus.class);

        return getElementRuntimeService()
                .getActiveRuntimes()
                .stream()
                .map(mapper::forward)
                .toList();

    }

    @Override
    public List<ElementContainerStatus> getAllContainers() {

        final var mapper = getMapperRegistry().getMapper(ContainerRecord.class, ElementContainerStatus.class);

        return getElementContainerService()
                .getActiveContainers()
                .stream()
                .map(mapper::forward)
                .toList();

    }

    public ElementRuntimeService getElementRuntimeService() {
        return elementRuntimeService;
    }

    @Inject
    public void setElementRuntimeService(ElementRuntimeService elementRuntimeService) {
        this.elementRuntimeService = elementRuntimeService;
    }

    public ElementContainerService getElementContainerService() {
        return elementContainerService;
    }

    @Inject
    public void setElementContainerService(ElementContainerService elementContainerService) {
        this.elementContainerService = elementContainerService;
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

}
