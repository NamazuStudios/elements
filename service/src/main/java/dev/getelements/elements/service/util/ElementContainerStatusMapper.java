package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.deployment.ElementContainerService.ContainerRecord;
import dev.getelements.elements.sdk.deployment.ElementContainerService.ContainerStatus;
import dev.getelements.elements.sdk.model.system.ElementContainerStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {ElementMetadataMapper.class, ElementRuntimeStatusMapper.class})
public interface ElementContainerStatusMapper extends MapperRegistry.Mapper<ContainerRecord, ElementContainerStatus> {

    @Override
    @Mapping(target = "status", expression = "java(mapStatus(source.status()))")
    @Mapping(target = "runtime", source = "runtime")
    @Mapping(target = "elements", source = "elements")
    ElementContainerStatus forward(ContainerRecord source);

    default String mapStatus(final ContainerStatus status) {
        return status != null ? status.name() : null;
    }

}
