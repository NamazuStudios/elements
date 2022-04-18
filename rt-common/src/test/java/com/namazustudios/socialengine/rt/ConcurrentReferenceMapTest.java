package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.util.ConcurrentReferenceMap.Builder;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class ConcurrentReferenceMapTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            new ConcurrentReferenceMapTest(new Builder<String, MockValue>().withSoftRef().build()),
            new ConcurrentReferenceMapTest(new Builder<String, MockValue>().withWeakRef().build())
        };
    }

    private final Map<String, MockValue> map;

    private final AtomicInteger counter = new AtomicInteger();

    private ConcurrentReferenceMapTest(final Map<String, MockValue> map) {
        this.map = map;
    }

    private String nextKey() {
        return format("key%d", counter.getAndIncrement());
    }

    @Test(threadPoolSize = 100, invocationCount = 1000)
    public void canAddAndRemoveKeyValuePairToAMap() {

        final var key1 = nextKey();
        final var key2 = nextKey();
        final var key3 = nextKey();

        final var value1 = new MockValue();
        final var value2 = new MockValue();
        final var value3 = new MockValue();

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

        assertEquals(map.get(key1), value1);
        assertEquals(map.get(key2), value2);
        assertEquals(map.get(key3), value3);

        map.remove(key1);
        map.remove(key2);
        map.remove(key3);

        assertNull(map.get(key1));
        assertNull(map.get(key2));
        assertNull(map.get(key3));

    }

    @Test(threadPoolSize = 100, invocationCount = 1000)
    public void cannotRemoveKeyValuePairwhenKeyIsWrongFromMap(){

        final var key1 = nextKey();
        final var key2 = nextKey();
        final var key3 = nextKey();

        assertNull(map.remove(key1));
        assertNull(map.remove(key2));
        assertNull(map.remove(key3));

    }

    @Test(threadPoolSize = 5000, invocationCount = 10000)
    public void testMapDoesNotOOMEStressTest() {

        final var key1 = nextKey();
        final var key2 = nextKey();
        final var key3 = nextKey();

        final var value1 = new MockValue();
        final var value2 = new MockValue();
        final var value3 = new MockValue();

        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);

    }

    private static class MockValue {
        private final byte[] blob = new byte[4096];
    }

}
