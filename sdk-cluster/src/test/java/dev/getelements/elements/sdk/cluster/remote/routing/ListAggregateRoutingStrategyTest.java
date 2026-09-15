package dev.getelements.elements.sdk.cluster.remote.routing;

import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class ListAggregateRoutingStrategyTest {

    private final ListAggregateRoutingStrategy strategy = new ListAggregateRoutingStrategy();

    @Test
    public void newInitialInvocationResultIsEmpty() {
        final var initial = strategy.newInitialInvocationResult();
        assertEquals(initial.result(), List.of());
        assertEquals(initial.param(), 0);
    }

    @Test
    public void combineCarriesFirstOperandsParam() {

        final var a = new InvocationResult(List.of("a"), 2);
        final var b = new InvocationResult(List.of("b"), 5);

        final var combined = strategy.combine(a, b);

        assertEquals(combined.result(), List.of("a", "b"));
        assertEquals(combined.param(), 2);

    }

}
