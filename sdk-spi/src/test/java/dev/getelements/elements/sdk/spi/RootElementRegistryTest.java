package dev.getelements.elements.sdk.spi;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.Event;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class RootElementRegistryTest {

    @Test
    public void unregisterRemovesTheRequestedElementNotJustTheFirst() {

        final var registry = new RootElementRegistry();
        final var first = mock(Element.class);
        final var second = mock(Element.class);
        final var third = mock(Element.class);

        registry.register(first);
        registry.register(second);
        registry.register(third);

        final var removed = registry.unregister(second);

        assertTrue(removed);
        assertEquals(registry.stream().toList(), java.util.List.of(first, third));

    }

    @Test
    public void unregisterReturnsFalseForAnElementThatWasNeverRegistered() {

        final var registry = new RootElementRegistry();
        final var registered = mock(Element.class);
        final var neverRegistered = mock(Element.class);

        registry.register(registered);

        final var removed = registry.unregister(neverRegistered);

        assertFalse(removed);
        assertEquals(registry.stream().toList(), java.util.List.of(registered));

    }

    @Test
    public void unregisterDetachesTheElementFromFurtherEventsAndRegistryClose() {

        final var registry = new RootElementRegistry();
        final var element = mock(Element.class);

        registry.register(element);
        registry.unregister(element);

        final var event = Event.builder().named("some.event").build();
        registry.publish(event);

        verify(element, never()).publish(event);

        registry.close();

        verify(element, never()).close();

    }

    @Test
    public void registeredElementIsStillClosedWhenRegistryCloses() throws Exception {

        final var registry = new RootElementRegistry();
        final var element = mock(Element.class);

        registry.register(element);
        registry.close();

        verify(element, times(1)).close();

    }

}
