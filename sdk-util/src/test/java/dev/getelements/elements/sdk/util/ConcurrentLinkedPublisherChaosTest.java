package dev.getelements.elements.sdk.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;

public class ConcurrentLinkedPublisherChaosTest {

    private final ConcurrentLinkedPublisher<Object> underTest = new ConcurrentLinkedPublisher<>();

    @Test(threadPoolSize = 500, invocationCount = 10000)
    public void testSubscribe() {
        underTest.subscribe((subscription, value) -> {
            assert value.equals("Test");
            subscription.unsubscribe();
        });
    }

    @Test(threadPoolSize = 500,
          invocationCount = 10000,
          dependsOnMethods = "testSubscribe")
    public void testPublishSimple() {
        underTest.publish("Test");
    }

    @Test(dependsOnMethods = "testPublishSimple")
    public void testPostPublishSimple() {
        var iterator = underTest.iterator();
        assertFalse(iterator.hasNext(), "Expected event bus to be empty.");
    }

}
