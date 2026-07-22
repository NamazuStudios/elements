package dev.getelements.elements.service.util;

import java.util.List;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService.FailedElementInfo;
import dev.getelements.elements.sdk.model.system.ElementMetadata;
import dev.getelements.elements.sdk.record.ElementRecord;

/**
 * Utility mapper for mapstruct.
 */
public class ElementMetadataMapper {

    public ElementMetadata map(final Element element) {
        return element == null ? null : ElementMetadata.from(element);
    }

    public List<ElementMetadata> mapElementMetadata(final List<Element> source) {
        return source == null
                ? List.of()
                : source.stream().map(this::map).toList();
    }

    public ElementMetadata mapRecord(final ElementRecord record) {
        return record == null ? null : ElementMetadata.from(record);
    }

    public List<ElementMetadata> mapFailedElements(final List<FailedElementInfo> source) {
        return source == null
                ? List.of()
                : source.stream()
                        .map(info -> ElementMetadata.from(info.record(), info.elementPath(), info.sourceElmArtifact()))
                        .toList();
    }

}
