package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.annotation.ElementEventConsumer;

public record ElementEventKey<ServiceT>(ElementServiceKey<ServiceT> serviceKey, String eventName) {

    public static <ServiceT> ElementEventKey<ServiceT> from(
            final ElementServiceKey<ServiceT> serviceKey,
            final ElementEventConsumer elementEventConsumer) {
        return new ElementEventKey<>(serviceKey, elementEventConsumer.value());
    }

}
