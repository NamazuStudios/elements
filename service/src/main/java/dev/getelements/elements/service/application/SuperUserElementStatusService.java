package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.PermittedPackages;
import dev.getelements.elements.sdk.PermittedTypes;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementContainerService.ContainerRecord;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.model.system.*;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.system.ElementStatusService;
import jakarta.inject.Inject;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public class SuperUserElementStatusService implements ElementStatusService {

    private MapperRegistry mapperRegistry;

    private ElementRegistry elementRegistry;

    private ElementRuntimeService elementRuntimeService;

    private ElementContainerService elementContainerService;

    @Override
    public List<ElementSpi> getAllBuiltinSpis() {
        return getElementRuntimeService().getBuiltinSpis();
    }

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

    @Override
    public List<ElementFeature> getAllFeatures() {

        final var types = ServiceLoader.load(PermittedTypes.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(pt -> new ElementFeature(pt.getClass().getName(), pt.getDescription()));

        final var packages = ServiceLoader.load(PermittedPackages.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .map(pp -> new ElementFeature(pp.getClass().getName(), pp.getDescription()));

        return Stream.concat(types, packages).toList();

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
