package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;
import org.testng.annotations.Test;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import static org.testng.Assert.assertEquals;

public class ConcurrentLinkedPublisherOrderTest {

    private final AtomicLong callCounter = new AtomicLong();

    private final Deque<Subscription> testSubscriptions = new ConcurrentLinkedDeque<>();

    private final ConcurrentLinkedPublisher<Object> underTest = new ConcurrentLinkedPublisher<>();

    @Test(threadPoolSize = 500, invocationCount = 10000)
    public void testSubscribe() {

        callCounter.incrementAndGet();

        final var subscription = underTest.subscribe(value -> {
            assert value.equals("Test");
            callCounter.decrementAndGet();
        });

        testSubscriptions.add(subscription);

    }

    @Test(dependsOnMethods = "testSubscribe")
    public void testPublishSimple() {
        underTest.publish("Test");
    }

    @Test(dependsOnMethods = "testPublishSimple")
    public void testPostPublishSimple() {
        assertEquals(callCounter.get(), 0);
    }

    @Test(
            threadPoolSize = 500,
            invocationCount = 10000,
            dependsOnMethods = "testPostPublishSimple")
    public void testUnsubscribe() {
        final var subscription = testSubscriptions.removeLast();
        subscription.unsubscribe();
    }

}
