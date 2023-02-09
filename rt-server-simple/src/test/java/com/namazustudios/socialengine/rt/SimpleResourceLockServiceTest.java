package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.ResourceLockService;
import com.namazustudios.socialengine.rt.SharedLock;
import com.namazustudios.socialengine.rt.SimpleResourceLockService;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.namazustudios.socialengine.rt.SimpleResourceLockService.getOrphanCount;
import static java.util.stream.Stream.generate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;

public class SimpleResourceLockServiceTest {

    private final ResourceLockService underTest = new SimpleResourceLockService();

    private final Map<ResourceId, SharedLock> locks = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] randomResourceIds() {

        generate(ResourceId::randomResourceId)
            .limit(500)
            .forEach(rid -> locks.put(rid, underTest.getLock(rid)));

        return locks
            .keySet()
            .stream()
            .map(rid -> new Object[]{rid})
            .toArray(Object[][]::new);

    }

    @Test(dataProvider = "randomResourceIds", threadPoolSize = 100, invocationCount = 10)
    public void stressTest(final ResourceId resourceId) {
        final var returned = underTest.getLock(resourceId);
        final var existing = locks.get(resourceId);
        assertSame(returned, existing, "Expected same lock.");
    }

    @Test(dependsOnMethods = "stressTest")
    public void deleteAll() {
        System.gc();
        locks.keySet().forEach(underTest::delete);
        assertEquals(0, underTest.size());
    }

    @Test(dependsOnMethods = "deleteAll")
    public void checkOrphans() {
        assertEquals(getOrphanCount(), 0);
    }

}
