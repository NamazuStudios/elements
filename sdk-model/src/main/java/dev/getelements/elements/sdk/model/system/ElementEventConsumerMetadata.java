package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.record.ElementEventConsumerRecord;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A DTO record for Element Event Consumer Metadata.
 */
public record ElementEventConsumerMetadata(
        String serviceType,
        String serviceName,
        String eventName,
        String methodName
) {

    public static ElementEventConsumerMetadata from(final ElementEventConsumerRecord<?> elementEventConsumerRecord) {
        return new ElementEventConsumerMetadata(
                elementEventConsumerRecord.eventKey().serviceKey().type().getName(),
                elementEventConsumerRecord.eventKey().serviceKey().name(),
                elementEventConsumerRecord.eventKey().eventName(),
                "%s.%s(%s)".formatted(
                        elementEventConsumerRecord.eventKey().serviceKey().type().getName(),
                        elementEventConsumerRecord.method().getName(),
                        Stream.of(elementEventConsumerRecord.method().getParameterTypes())
                                .map(Class::getSimpleName)
                                .collect(Collectors.joining(","))
                )
        );
    }

}
