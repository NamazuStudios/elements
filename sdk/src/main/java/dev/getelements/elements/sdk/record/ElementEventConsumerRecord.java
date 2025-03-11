package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * Represents a consumer method for an {@link Event}.
 *
 * @param eventKey the {@link ElementEventKey}
 * @param method the {@link Method} to receive the {@link Event}
 */
public record ElementEventConsumerRecord<ServiceT>(ElementEventKey<ServiceT> eventKey, Method method) {

    /**
     * Returns true if the method is a direct-dispatch method. Direct-dispatch methods will accept the {@link Event}
     * directly instead of relying on parameter matching. Direct-dispatch methods have a single parameter accepting
     * an instance of {@link Event}. ALl other methods will be ignored.
     *
     * @return true if direct-dispatch, false otherwise
     */
    public boolean isDirectDispatch() {
        final var parameters = method().getParameterTypes();
        return parameters.length == 1 && parameters[0] == Event.class;
    }

    /**
     * Creates a {@link Stream} of {@link ElementServiceRecord}s from the supplied {@link Method} and records
     *
     * @param elementServiceRecord the {@link ElementServiceRecord}
     * @param method the {@link Method} to use
     * @return the {@link Stream} of {@link ElementServiceRecord} instances
     */
    public static Stream<ElementEventConsumerRecord<?>> from(
            final ElementServiceRecord elementServiceRecord,
            final Method method) {
        return ElementServiceKey
                .from(elementServiceRecord)
                .flatMap(esk -> Stream
                        .of(method.getAnnotation(ElementEventConsumer.class))
                        .map(annotation -> ElementEventKey.from(esk, annotation))
                        .map(ek -> new ElementEventConsumerRecord<>(ek, method))
                );
    }

}
