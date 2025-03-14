package dev.getelements.elements.sdk.test.element.b;

import dev.getelements.elements.sdk.ElementRegistrySupplier;
import dev.getelements.elements.sdk.ElementSupplier;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.test.element.TestService;

import java.util.ArrayList;
import java.util.List;

@ElementServiceImplementation
@ElementServiceExport(TestService.class)
public class TestServiceImplementation implements TestService {

    private final List<Event> events = new ArrayList<>();

    private final List<Object> eventObjects = new ArrayList<>();

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
