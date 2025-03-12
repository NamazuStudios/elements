package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.application.Application;

import java.util.ServiceLoader;

/**
 * A builder type for the {@link ElementsLocal} instance.
 */
public interface ElementsLocalBuilder {

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param aPacakge            the name of the Java package for the Element
     * @return this instance
     */
    ElementsLocalBuilder withElementFromPacakge(String applicationNameOrId, String aPacakge);

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param aPackage            the {@link Package} for the Element
     * @return this instance
     */
    ElementsLocalBuilder withElementFromPackage(String applicationNameOrId, Package aPackage);

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
