package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.annotation.ElementServiceReference;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.regex.Pattern.quote;

/**
 * A record type which represents a key for a service.
 *
 * @param type the type of the service
 * @param name the name of the service
 * @param <ServiceT> the type of the service
 */
public record ElementServiceKey<ServiceT>(Class<ServiceT> type, String name) {

    /**
     * Specifies the name separator.
     */
    public static final String TYPE_DESIGNATION = "service";

    /**
     * Service name separator.
     */
    public static final String SERVICE_NAME_SEPARATOR = "#";

    public ElementServiceKey {
        name = name == null ? "" : name;
    }

    /**
     * Returns true if the service is named.
     *
     * @return true if named, false otherwise
     */
    public boolean isNamed() {
        return !name.isEmpty();
    }

    /**
     * Forms an instance of {@link ElementServiceKey} from an instance of {@link ElementServiceReference}.
     *
     * @param elementServiceReference the reference
     * @return the key
     */
    public static ElementServiceKey<?> from(final ElementServiceReference elementServiceReference) {
        return new ElementServiceKey<>(elementServiceReference.value(), elementServiceReference.name());
    }

    /**
     * Gets all {@link ElementServiceKey} isntances fom a single {@link ElementServiceRecord}.
     *
     * @param elementServiceRecord the {@link ElementServiceRecord}
     * @return the {@link ElementServiceKey}
     */
    public static Stream<ElementServiceKey<?>> from(final ElementServiceRecord elementServiceRecord) {
        final var name = elementServiceRecord.export().name();
        return elementServiceRecord.exposedTypes().map(type -> new ElementServiceKey<>(type, name));
    }

    /**
     * Parses the string representing the {@link ElementServiceKey}.
     *
     * @param elementRecord  the record representing the{@link Element}.
     * @param designatedServiceKeyString the {@link String} representing the {@link ElementServiceKey}
     * @return the {@link ElementServiceKey}
     */
    public static Optional<ElementServiceKey<?>> tryParse(
            final ElementRecord elementRecord,
            final String designatedServiceKeyString) {
        return ElementDesignationRecord
                .tryParse(designatedServiceKeyString)
                .filter(designation -> TYPE_DESIGNATION.equals(designation.designation()))
                .map(ElementDesignationRecord::value)
                .map(sks -> sks.split(quote(SERVICE_NAME_SEPARATOR)))
                .filter(components -> components.length == 0 || components.length == 1)
                .flatMap(components -> {
                    if (components.length == 0) {
                        final var aClass = tryLoadClass(elementRecord, components[0]);
                        return aClass.isEmpty()
                                ? Optional.empty()
                                : Optional.of(new ElementServiceKey<>(aClass.get(), ""));
                    } else {
                        final var aClass = tryLoadClass(elementRecord, components[0]);
                        return aClass.isEmpty()
                                ? Optional.empty()
                                : Optional.of(new ElementServiceKey<>(aClass.get(), components[1]));
                    }
                });
    }

    private static Optional<Class<?>> tryLoadClass(
            final ElementRecord elementRecord,
            final String aClassName) {
        try {
            return Optional.of(elementRecord.classLoader().loadClass(aClassName));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        }
    }

}
