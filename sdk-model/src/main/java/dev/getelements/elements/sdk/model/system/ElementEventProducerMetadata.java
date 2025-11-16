package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementEventProducerRecord;

import java.util.List;

/**
 * A DTO record for Element Event Producer Metadata.
 *
 * @param name the name of the event
 * @param description the description of the event
 * @param parameters the parameter types of the event
 */
public record ElementEventProducerMetadata(String name, String description, List<String> parameters) {

    /**
     * Convenience method to construct an ElementEventProducerMetadata from an ElementEventProducerRecord.
     * @param elementEventProducerRecord the ElementEventProducerRecord
     * @return the newly created ElementEventProducerMetadata
     */
    public static ElementEventProducerMetadata from(final ElementEventProducerRecord elementEventProducerRecord) {
        return new ElementEventProducerMetadata(
                elementEventProducerRecord.name(),
                elementEventProducerRecord.description(),
                elementEventProducerRecord.parameters().stream()
                        .map(Class::getName)
                        .toList()
        );
    }

}
