    package dev.getelements.elements.sdk.test.element.a;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.test.element.TestService;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TestServiceImplementation implements TestService {

    private static final List<Event> events = new CopyOnWriteArrayList<>();

    private static final List<MethodEventRecord> eventObjects = new CopyOnWriteArrayList<>();

    @ElementDefaultAttribute(value = "test.value", description = "A test configuration parameter.")
    public static final String TEST_CONFIGURATION_PARAMETER = "dev.getelements.elements.sdk.test.element.a.config";

    @Override
    public String getImplementationPackage() {
        return getClass().getPackage().getName();
    }

    @Override
    public void testElementSpi() {
        final var element = ElementSupplier.getElementLocal(getClass()).get();
        final var elementName = element.getElementRecord().definition().name();
        assert elementName.equals(getImplementationPackage());
    }

    @Override
    public void testElementRegistrySpi() {
        final var element = ElementSupplier.getElementLocal(getClass()).get();
        final var elementRegistrySupplier = ElementRegistrySupplier.getElementLocal(TestServiceImplementation.class);
        final var elementRegistry = elementRegistrySupplier.get();
        assert element == elementRegistry.find(element.getElementRecord().definition().name()).findFirst().get();
    }

    @Override
    public List<Event> getConsumedEvents() {
        return events;
    }

    @Override
    public List<MethodEventRecord> getConsumedEventObjects() {
        return eventObjects;
    }

    @ElementEventConsumer(ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED)
    public void elementLoaded(Event event) {
        events.add(event);
    }

    @ElementEventConsumer(TestService.TEST_ELEMENT_EVENT_1)
    public void testEvent1ConsumerWithObject(String value1, String value2) {
        final var record = new MethodEventRecord(TEST_ELEMENT_EVENT_1, List.of(value1, value2));
        eventObjects.add(record);
    }

    @ElementEventConsumer(TestService.TEST_ELEMENT_EVENT_2)
    public void testEvent2ConsumerWithObject(String value1, String value2) {
        final var record = new MethodEventRecord(TEST_ELEMENT_EVENT_2, List.of(value1, value2));
        eventObjects.add(record);
    }

}
