package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementType;

import java.util.List;
import java.util.Optional;

/**
 * A Record type encapsulating all of the metadata for a particular {@link Element}. This is derived from various
 * sources, which are mostly reflections of the annotation.
 *
 * @param type the {@link ElementType}
 * @param definition the {@link ElementDefinitionRecord} housing the definition of the Element
 * @param services a list of {@link ElementServiceRecord} housing all services in the Element
 * @param attributes the actual loaded {@link Attributes} for the Element
 * @param defaultAttributes the default attributes of the {@link Element}
 * @param classLoader the {@link ClassLoader} used to load the {@link Element}
 */
public record ElementRecord(
        ElementType type,
        ElementDefinitionRecord definition,
        List<ElementServiceRecord> services,
        List<ElementEventProducerRecord> producedEvents,
        List<ElementEventConsumerRecord<?>> consumedEvents,
        List<ElementDependencyRecord> dependencies,
        Attributes attributes,
        List<ElementDefaultAttributeRecord> defaultAttributes,
        ClassLoader classLoader,
        List<ElementSpiImplementationRecord> spis) {

    public ElementRecord {
        services = List.copyOf(services);
        consumedEvents = List.copyOf(consumedEvents);
        defaultAttributes = List.copyOf(defaultAttributes);
        dependencies = List.copyOf(dependencies);
    }

    /**
     * Checks if the supplied {@link Class} is part of the {@link Element} attached to this record.
     *
     * @param aClass a {@link Class}
     * @return true if part of this {@link Element}, false otherwise
     */
    public boolean isPartOfElement(final Class<?> aClass) {
        return definition().isPartOfElement(aClass) || spis()
                .stream()
                .anyMatch(spi -> spi.isPartOfElement(aClass));
    }

    /**
     * Parses the string representing the {@link ElementServiceKey}.
     *
     * @param serviceKeyString the {@link String} representing the {@link ElementServiceKey}
     * @return the {@link ElementServiceKey}
     */
    public Optional<ElementServiceKey<?>> tryParseServiceKey(final String serviceKeyString) {
        return ElementServiceKey.tryParse(this, serviceKeyString);
    }

}
