package dev.getelements.elements.sdk.test.element;

import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementPublic;

import java.util.List;

/**
 * A test service.
 */
@ElementPublic
public interface TestService {

    String TEST_ELEMENT_EVENT_1 = "dev.getelements.elements.element.test.event.1";

    String TEST_ELEMENT_EVENT_2 = "dev.getelements.elements.element.test.event.2";

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
