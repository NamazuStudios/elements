package dev.getelements.elements.service.util;

import java.util.List;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.model.system.ElementMetadata;

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

}
