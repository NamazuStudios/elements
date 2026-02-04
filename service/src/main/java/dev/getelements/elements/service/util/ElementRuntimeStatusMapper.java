package dev.getelements.elements.service.util;

import dev.getelements.elements.common.app.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.common.app.ElementRuntimeService.RuntimeStatus;
import dev.getelements.elements.sdk.model.system.ElementRuntimeStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {ElementMetadataMapper.class})
public interface ElementRuntimeStatusMapper extends MapperRegistry.Mapper<RuntimeRecord, ElementRuntimeStatus> {

    @Override
    ElementRuntimeStatus forward(RuntimeRecord source);

}