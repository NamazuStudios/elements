package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementType;
import dev.getelements.elements.sdk.record.ElementDefaultAttributeRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.sdk.record.ElementRequiredAttributeRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.record.ElementDefaultAttributeRecord.REDACTED;

/**
 * Record type for the {@link ElementMetadata}. This contains a summary of all the metadata in an {@link ElementRecord}
 * but only types which can be serialized (no ClassLoaders, etc).
 *
 * @param type the type of {@link dev.getelements.elements.sdk.Element}
 * @param definition the definition of the {@link dev.getelements.elements.sdk.Element}
 * @param services the services provided by the {@link dev.getelements.elements.sdk.Element}
 * @param producedEvents the events produced by the {@link dev.getelements.elements.sdk.Element}
 * @param consumedEvents the events consumed by the {@link dev.getelements.elements.sdk.Element}
 * @param dependencies the dependencies of the {@link dev.getelements.elements.sdk.Element}
 * @param attributes the attributes of the {@link dev.getelements.elements.sdk.Element}
 * @param defaultAttributes the default attributes of the {@link dev.getelements.elements.sdk.Element}
 * @param requiredAttributes the required attributes of the {@link dev.getelements.elements.sdk.Element}
 * @param elementPath the on-disk directory path of the element within its deployment (only set for failed elements;
 *                    null for successfully loaded elements). This is the exact key to use in {@code pathAttributes}
 *                    to configure attributes for this element.
 * @param sourceElmArtifact the Maven coordinates of the package ELM that contributed this element, or {@code null}
 *                          if the element came from the deployment's own ELM file or an element definition.
 *                          Only set for failed elements.
 */
public record ElementMetadata(
        ElementType type,
        ElementDefinitionMetadata definition,
        List<ElementServiceMetadata> services,
        List<ElementEventProducerMetadata> producedEvents,
        List<ElementEventConsumerMetadata> consumedEvents,
        List<ElementDependencyMetadata> dependencies,
        Map<String, Object> attributes,
        List<ElementDefaultAttributeRecord> defaultAttributes,
        List<ElementRequiredAttributeRecord> requiredAttributes,
        String elementPath,
        String sourceElmArtifact) {

    /**
     * Convenience method to construct an ElementMetadata from an {@link Element}.
     *
     * @param element the {@link Element}
     * @return the newly created {@link ElementMetadata}
     */
    public static ElementMetadata from(final Element element) {
        return from(element.getElementRecord());
    }

    /**
     * Convenience method to construct an ElementMetadata from an {@link ElementRecord}.
     *
     * @param element the {@link ElementRecord}
     * @return the newly created {@link ElementMetadata}
     */
    public static ElementMetadata from(final ElementRecord element) {
        return from(element, null, null);
    }

    /**
     * Constructs an ElementMetadata from an {@link ElementRecord} with the on-disk element path and source package.
     * Use this overload for failed elements where the path and source are known.
     *
     * @param element the {@link ElementRecord}
     * @param elementPath the directory path of the element within the deployment (leading slashes stripped)
     * @param sourceElmArtifact the Maven coordinates of the source package, or {@code null} for ELM/element-definition elements
     * @return the newly created {@link ElementMetadata}
     */
    public static ElementMetadata from(final ElementRecord element, final String elementPath, final String sourceElmArtifact) {

        final var attributesMap = new LinkedHashMap<>(element.attributes().asMap());

        final var defaultAttributes = element.defaultAttributes()
                        .stream()
                        .map(ElementDefaultAttributeRecord::redacted)
                        .toList();

        defaultAttributes
                .stream()
                .filter(ElementDefaultAttributeRecord::sensitive)
                .forEach(attribute -> attributesMap.put(attribute.name(), REDACTED));

        return new ElementMetadata(
                element.type(),
                ElementDefinitionMetadata
                        .from(element.definition()),
                element.services()
                        .stream()
                        .map(ElementServiceMetadata::from)
                        .toList(),
                element.producedEvents()
                        .stream()
                        .map(ElementEventProducerMetadata::from)
                        .toList(),
                element.consumedEvents()
                        .stream()
                        .map(ElementEventConsumerMetadata::from)
                        .toList(),
                element.dependencies()
                        .stream()
                        .map(ElementDependencyMetadata::from)
                        .toList(),
                attributesMap,
                element.defaultAttributes()
                        .stream()
                        .map(ElementDefaultAttributeRecord::redacted)
                        .toList(),
                element.requiredAttributes(),
                elementPath,
                sourceElmArtifact
        );

    }

}
