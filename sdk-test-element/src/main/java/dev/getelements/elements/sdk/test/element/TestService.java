package dev.getelements.elements.sdk.test.element;

import dev.getelements.elements.sdk.annotation.ElementPublic;

/**
 * A test service.
 */
@ElementPublic
public interface TestService {

    /**
     * Returns the implementation's package.
     *
     * @return the implementation's package name.
     */
    String getImplementationPackage();

    /**
     * Attempts to get the element SPI.
     */
    void testElementSpi();

    /**
     * Attempts to get the element registry SPI.
     */
    void testElementRegistrySpi();

}
