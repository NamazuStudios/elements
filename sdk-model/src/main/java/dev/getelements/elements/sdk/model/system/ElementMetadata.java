package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementType;
import dev.getelements.elements.sdk.record.*;

import java.util.List;
import java.util.Map;

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
 */
public record ElementMetadata(
        ElementType type,
        ElementDefinitionMetadata definition,
        List<ElementServiceRecord> services,
        List<ElementEventProducerRecord> producedEvents,
        List<ElementEventConsumerRecord<?>> consumedEvents,
        List<ElementDependencyRecord> dependencies,
        Map<String, Object> attributes,
        List<ElementDefaultAttributeRecord> defaultAttributes) {

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
     * Convenience method to construct and ElementMetadata from an {@link ElementRecord}.
     *
     * @param element the {@link ElementRecord}
     * @return the newly created {@link ElementMetadata}
     */
    public static ElementMetadata from(final ElementRecord element) {
        return new ElementMetadata(
                element.type(),
                ElementDefinitionMetadata.from(element.definition()),
                element.services(),
                element.producedEvents(),
                element.consumedEvents(),
                element.dependencies(),
                element.attributes().asMap(),
                element.defaultAttributes()
        );
    }

}
