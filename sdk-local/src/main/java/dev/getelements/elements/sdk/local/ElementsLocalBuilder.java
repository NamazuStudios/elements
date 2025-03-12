package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.util.PropertiesAttributes;

import java.util.Properties;
import java.util.ServiceLoader;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;

/**
 * A builder type for the {@link ElementsLocal} instance.
 */
public interface ElementsLocalBuilder {

    /**
     * Specifies the system properties as a {@link Properties} instance.
     *
     * @param properties properties
     * @return this instance
     */
    default ElementsLocalBuilder withProperties(Properties properties) {
        final var attributes = new PropertiesAttributes(properties);
        return withAttributes(attributes);
    }

    /**
     * Specifies the system properties as an {@link Attributes} instance.
     *
     * @param attributes the attributes
     * @return this instance
     */
    ElementsLocalBuilder withAttributes(Attributes attributes);

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param aPacakge            the name of the Java package for the Element
     * @return this instance
     */
    default ElementsLocalBuilder withElementFromPacakge(
            final String applicationNameOrId,
            final String aPacakge) {
        return withElementFromPacakge(applicationNameOrId, aPacakge, emptyAttributes());
    }

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param aPacakge            the name of the Java package for the Element\
     * @param attributes the {@link Attributes} to use when loading the package
     * @return this instance
     */
    ElementsLocalBuilder withElementFromPacakge(
            String applicationNameOrId,
            String aPacakge,
            Attributes attributes);
    
    /**
     * Builds the {@link ElementsLocal} instance
     *
     * @return the {@link ElementsLocal} instance
     */
    ElementsLocal build();

    /**
     * Gets a default instance of the {@link ElementsLocalBuilder}.
     *
     * @return the {@link ElementsLocalBuilder}
     */
    static ElementsLocalBuilder getDefault() {
        return ServiceLoader
                .load(ElementsLocalBuilder.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No SPI for " + ElementsLocalBuilder.class.getName() + " found."))
                .get();
    }

}
