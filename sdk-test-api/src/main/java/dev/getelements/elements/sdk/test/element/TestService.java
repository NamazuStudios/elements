package dev.getelements.elements.sdk.test.element;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementPublic;

import java.util.List;

/**
 * A test service.
 */
@ElementPublic
public interface TestService {

    /**
     * Shared attribute key used to verify that each element's {@code @ElementDefaultAttribute} is scoped
     * to that element and does not bleed into sibling elements loaded from the same classpath.
     */
    String SHARED_DEFAULT_ATTR = "dev.getelements.elements.sdk.test.element.shared.default";

    /**
     * A server-level default declared in this non-element interface, simulating the production
     * scenario where server framework code declares an {@code @ElementDefaultAttribute} (e.g.
     * {@code auth.enabled=false}) while a custom element re-declares the same key with a different
     * default (e.g. {@code auth.enabled=true}).  The element's declaration must win when the
     * operator has not explicitly overridden the key.
     */
    @ElementDefaultAttribute(value = "server-default", description = "Server-level default that elements may override.")
    String SERVER_OVERRIDABLE_ATTR = "dev.getelements.elements.sdk.test.element.server.overridable";

    String TEST_ELEMENT_EVENT_1 = "dev.getelements.elements.element.test.event.1";

    String TEST_ELEMENT_EVENT_2 = "dev.getelements.elements.element.test.event.2";

    String TEST_ELEMENT_EVENT_3 = "dev.getelements.elements.element.test.event.3";

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

    /**
     * Gets the events registered for consumption and consumed
     * @return A list of consumed events
     */
    List<Event> getConsumedEvents();

    /**
     * Gets the objects passed into the consumed events
     * @return the event objects
     */
    List<MethodEventRecord> getConsumedEventObjects();

    /**
     * Represents an event record, capturing name and arguments.
     *
     * @param name the event name
     * @param arguments the arguments
     */
    @ElementPublic
    record MethodEventRecord(String name, List<Object> arguments) {}

}
