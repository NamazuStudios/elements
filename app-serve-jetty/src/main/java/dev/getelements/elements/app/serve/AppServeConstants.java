package dev.getelements.elements.app.serve;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementDefinition;

/**
 * Constants for the Application Server (often times called appserve) component.
 */
public interface AppServeConstants {

    /**
     * Defines an attribute which specifies the prefix for the element. At laod-time, loader will inspect the
     * {@link Attributes} from the {@link Element}. If blank ({@see {@link String#isBlank()}}, then the loader will
     * defer to the value of {@link ElementDefinition#value()}, which would typically be the name of the package
     * bearing the annotation.
     */
    @ElementDefaultAttribute("")
    String APPLICATION_PREFIX = "dev.getelements.elements.app.serve.prefix";

    /**
     * Defines an attribute which specifies if the elements should enable the standard auth pipeline in Elements.
     * This ensures that the application server will be able to authenticate users using the Authorization or
     * Elements-SessionSecret headers as well as allow the appropriate override headers to be used.
     */
    @ElementDefaultAttribute("")
    String ENABLE_ELEMENTS_AUTH = "dev.getelements.elements.auth.enabled";

}
