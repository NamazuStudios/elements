package com.namazustudios.socialengine.rt;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class SimplePublisherTest {

    private static SimplePublisher<Object> sharedTestObject;

    @BeforeClass
    public void createSharedInstance() {
        sharedTestObject = new SimplePublisher<>();
    }

    @BeforeMethod
    public void clearSharedTestObject() {
        sharedTestObject.clear();
    }

    @DataProvider
    public Object[][] getObjectToTest() {
        return new Object[][] {
            new Object[]{ new SimplePublisher<>()},
            new Object[]{ sharedTestObject }
        };
    }

    @Test(dataProvider = "getObjectToTest")
    public void testSubscribeUnsubscribeConsumer(final SimplePublisher<Object> objectSimplePublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final Subscription subscription = objectSimplePublisher.subscribe(objectConsumer);
            assertNotNull(subscription);

            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));

            reset(objectConsumer);
            subscription.unsubscribe();
            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));

        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testSubscribeUnsubscribeBiConsumer(final SimplePublisher<Object> objectSimplePublisher) {


        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final BiConsumer<Subscription, Object> objectConsumer = mock(BiConsumer.class);
            final Subscription subscription = objectSimplePublisher.subscribe(objectConsumer);
            assertNotNull(subscription);

            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer);
            subscription.unsubscribe();
            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(any(Subscription.class), same(msg));

        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testSubscribeUnsubscribeBiConsumerMixed(final SimplePublisher<Object> objectSimplePublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            final Subscription first = objectSimplePublisher.subscribe(objectConsumer);
            final Subscription second = objectSimplePublisher.subscribe(subscriptionObjectBiConsumer);

            assertNotNull(first);
            assertNotNull(second);

            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer, subscriptionObjectBiConsumer);

            first.unsubscribe();
            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer, subscriptionObjectBiConsumer);

            second.unsubscribe();
            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(0)).accept(any(Subscription.class), same(msg));
        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testDoubleRemove(final SimplePublisher<Object> objectSimplePublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 100; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            final Subscription first = objectSimplePublisher.subscribe(objectConsumer);
            final Subscription second = objectSimplePublisher.subscribe(subscriptionObjectBiConsumer);

            assertNotNull(first);
            assertNotNull(second);

            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(objectConsumer, subscriptionObjectBiConsumer);

            first.unsubscribe();
            first.unsubscribe();
            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testClear(final SimplePublisher<Object> objectSimplePublisher) {

        final Object msg = new Object();

        for (int i = 0; i < 10; ++i) {

            final Consumer<Object> objectConsumer = mock(Consumer.class);
            final BiConsumer<Subscription, Object> subscriptionObjectBiConsumer = mock(BiConsumer.class);

            objectSimplePublisher.subscribe(objectConsumer);
            objectSimplePublisher.subscribe(subscriptionObjectBiConsumer);

            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(1)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            objectSimplePublisher.clear();

            reset(objectConsumer, subscriptionObjectBiConsumer);

            objectSimplePublisher.publish(msg);
            verify(objectConsumer, times(0)).accept(same(msg));
            verify(subscriptionObjectBiConsumer, times(0)).accept(any(Subscription.class), same(msg));
        }

    }

    @Test(dataProvider = "getObjectToTest")
    public void testUnsubscribeDuringIteration(final SimplePublisher<Object> objectSimplePublisher) throws NoSuchFieldException, IllegalAccessException {

        final Object msg = new Object();

        final Field first = SimplePublisher.class.getDeclaredField("first");
        final Field last = SimplePublisher.class.getDeclaredField("last");
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

            objectSimplePublisher.subscribe(subscriptionObjectBiConsumer);
            objectSimplePublisher.publish(msg);
            verify(subscriptionObjectBiConsumer, times(1)).accept(any(Subscription.class), same(msg));

            reset(subscriptionObjectBiConsumer);
            objectSimplePublisher.publish(msg);
            verify(subscriptionObjectBiConsumer, times(0)).accept(any(Subscription.class), same(msg));

            assertEquals(last.get(objectSimplePublisher), first.get(objectSimplePublisher));

        }

    }

}
