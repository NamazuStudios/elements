package dev.getelements.elements.sdk.test.element.a;

import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementEventProducer;
import dev.getelements.elements.sdk.test.element.TestService;

import java.util.ArrayList;
import java.util.List;

public class TestServiceImplementation implements TestService {

    private final List<Event> events = new ArrayList<>();

    private final List<Object> eventObjects = new ArrayList<>();

    @ElementDefaultAttribute("test.value")
    public static final String TEST_CONFIGURATION_PARAMETER = "dev.getelements.elements.sdk.test.element.a.config";

    static {
        System.out.println();
    }

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
    public List<Object> getConsumedEventObjects() {
        return eventObjects;
    }

    @ElementEventConsumer(Event.SYSTEM_EVENT_ELEMENT_LOADED)
    public void elementLoaded(Event event) {
        events.add(event);
    }

    @ElementEventConsumer(TestService.TEST_ELEMENT_EVENT)
    public void testEventConsumerWithObject(String value1, String value2) {
        eventObjects.add(value1);
        eventObjects.add(value2);
    }
}
