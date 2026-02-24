package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.model.system.ElementManifestMetadata;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.record.ElementManifestRecord;
import org.mapstruct.Mapper;

@Mapper
public interface ElementManifestMapper extends MapperRegistry.Mapper<ElementManifestRecord, ElementManifestMetadata> {

    @Override
    ElementManifestMetadata forward(ElementManifestRecord source);

}
