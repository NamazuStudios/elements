package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.application.Application;

import java.util.Properties;
import java.util.ServiceLoader;

/**
 * A builder type for the {@link ElementsLocal} instance. This builder type accepts only standard Java types because
 * it may load elements from a separate classloader.
 */
public interface ElementsLocalBuilder {

    /**
     * Specifies the system properties as a {@link Properties} instance.
     *
     * @param attributes properties
     * @return this instance
     */
    ElementsLocalBuilder withAttributes(Properties attributes);

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param aPacakge            the name of the Java package for the Element
     * @return this instance
     */
    default ElementsLocalBuilder withElementNamed(
            final String applicationNameOrId,
            final String aPacakge) {
        return withElementNamed(applicationNameOrId, aPacakge, new Properties());
    }

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param elementName            the name of the Element
     * @param attributes the {@link Attributes} to use when loading the package
     * @return this instance
     */
    ElementsLocalBuilder withElementNamed(
            String applicationNameOrId,
            String elementName,
            Properties attributes);

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
