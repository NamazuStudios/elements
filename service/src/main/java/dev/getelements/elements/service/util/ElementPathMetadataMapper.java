package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.model.system.ElementPathRecordMetadata;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.record.ElementPathRecord;
import org.mapstruct.Mapper;

import java.nio.file.Path;
import java.util.Map;

@Mapper(uses = {ElementManifestMapper.class})
public interface ElementPathMetadataMapper extends MapperRegistry.Mapper<ElementPathRecord, ElementPathRecordMetadata> {

    @Override
    ElementPathRecordMetadata forward(final ElementPathRecord source);

    default String map(final Path path) {
        return path.toString();
    }

    default Map<String, Object> map(final Attributes attributes) {
        return attributes.asMap();
    }

}
