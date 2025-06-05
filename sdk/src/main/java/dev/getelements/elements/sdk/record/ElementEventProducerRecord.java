package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementEventProducer;

import java.util.List;

public record ElementEventProducerRecord(String name, String description, List<Class<?>> parameters) {

    public ElementEventProducerRecord {
        parameters = List.copyOf(parameters);
    }

    public static ElementEventProducerRecord from(final ElementEventProducer producer) {
        return new ElementEventProducerRecord(
                producer.value(),
                producer.description(),
                List.of(producer.parameters())
        );
    }

}
