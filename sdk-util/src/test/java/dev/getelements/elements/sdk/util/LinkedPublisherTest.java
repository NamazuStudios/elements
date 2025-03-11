package dev.getelements.elements.sdk.util;

import dev.getelements.elements.sdk.Subscription;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class LinkedPublisherTest {

    private static LinkedPublisher<Object> sharedTestObject;

    @BeforeClass
    public void createSharedInstance() {
        sharedTestObject = new LinkedPublisher<>();
    }

    @BeforeMethod
    public void clearSharedTestObject() {
        sharedTestObject.clear();
    }

    @DataProvider
    public Object[][] getObjectToTest() {
        return new Object[][] {
            new Object[]{ new LinkedPublisher<>()},
            new Object[]{ sharedTestObject }
        };
    }

    @Test(dataProvider = "getObjectToTest")
    public void testSubscribeUnsubscribeConsumer(final LinkedPublisher<Object> objectLinkedPublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final Subscription subscription = objectLinkedPublisher.subscribe(objectConsumer);
            assertNotNull(subscription);

            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));

            reset(objectConsumer);
            subscription.unsubscribe();
            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));

        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testSubscribeUnsubscribeBiConsumer(final LinkedPublisher<Object> objectLinkedPublisher) {


        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final BiConsumer<Subscription, Object> objectConsumer = mock(BiConsumer.class);
            final Subscription subscription = objectLinkedPublisher.subscribe(objectConsumer);
            assertNotNull(subscription);

            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer);
            subscription.unsubscribe();
            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(any(Subscription.class), same(msg));

        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testSubscribeUnsubscribeBiConsumerMixed(final LinkedPublisher<Object> objectLinkedPublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            final Subscription first = objectLinkedPublisher.subscribe(objectConsumer);
            final Subscription second = objectLinkedPublisher.subscribe(subscriptionObjectBiConsumer);

            assertNotNull(first);
            assertNotNull(second);

            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer, subscriptionObjectBiConsumer);

            first.unsubscribe();
            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer, subscriptionObjectBiConsumer);

            second.unsubscribe();
            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(0)).accept(any(Subscription.class), same(msg));
        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testDoubleRemove(final LinkedPublisher<Object> objectLinkedPublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 100; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            final Subscription first = objectLinkedPublisher.subscribe(objectConsumer);
            final Subscription second = objectLinkedPublisher.subscribe(subscriptionObjectBiConsumer);

            assertNotNull(first);
            assertNotNull(second);

            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer, subscriptionObjectBiConsumer);

            first.unsubscribe();
            first.unsubscribe();
            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testClear(final LinkedPublisher<Object> objectLinkedPublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            objectLinkedPublisher.subscribe(objectConsumer);
            objectLinkedPublisher.subscribe(subscriptionObjectBiConsumer);

            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            objectLinkedPublisher.clear();

            reset(objectConsumer, subscriptionObjectBiConsumer);

            objectLinkedPublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(0)).accept(any(Subscription.class), same(msg));
        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testUnsubscribeDuringIteration(final LinkedPublisher<Object> objectLinkedPublisher) throws NoSuchFieldException, IllegalAccessException {

        final Object msg = new Object();

        final Field first = LinkedPublisher.class.getDeclaredField("first");
        final Field last = LinkedPublisher.class.getDeclaredField("last");
        first.setAccessible(true);
        last.setAccessible(true);

        for (int i = 0; i < 10; ++i) {

            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            doAnswer(invocation -> {
                final Subscription s = invocation.getArgument(0);
                final Object object = invocation.getArgument(1);
                s.unsubscribe();
                assertSame(msg, object);
                return null;
            }).when(subscriptionObjectBiConsumer).accept(any(), any());

            objectLinkedPublisher.subscribe(subscriptionObjectBiConsumer);
            objectLinkedPublisher.publish(msg);
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(subscriptionObjectBiConsumer);
            objectLinkedPublisher.publish(msg);
            verify(subscriptionObjectBiConsumer, times(0)).accept(any(Subscription.class), same(msg));

            assertEquals(last.get(objectLinkedPublisher), first.get(objectLinkedPublisher));

        }

    }

}
