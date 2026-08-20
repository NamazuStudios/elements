package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import jakarta.inject.Named;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.LargeObjectDao.*;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoLargeObjectDaoTest {

    private LargeObjectTestFactory largeObjectTestFactory;
    private LargeObjectDao largeObjectDao;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private final Set<String> createdEventIds = ConcurrentHashMap.newKeySet();

    private final Set<String> updatedEventIds = ConcurrentHashMap.newKeySet();

    private final Set<String> deletedEventIds = ConcurrentHashMap.newKeySet();

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            switch (ev.getEventName()) {
                case LARGE_OBJECT_CREATED -> createdEventIds.add(ev.getEventArgument(0, LargeObject.class).getId());
                case LARGE_OBJECT_UPDATED -> updatedEventIds.add(ev.getEventArgument(0, LargeObject.class).getId());
                case LARGE_OBJECT_DELETED -> deletedEventIds.add(ev.getEventArgument(0, LargeObject.class).getId());
            }
        });
    }

    @Test
    public void testCreateGetAndDeleteLargeObject() {
        final LargeObject largeObject = largeObjectTestFactory.createDefaultLargeObject(largeObjectTestFactory.wildcardAccess());

        LargeObject createdLargeObject = largeObjectDao.createLargeObject(largeObject);
        LargeObject foundLargeObject = largeObjectDao.getLargeObject(createdLargeObject.getId());
        largeObjectDao.deleteLargeObject(createdLargeObject.getId());
        Optional<LargeObject> foundDeleted = largeObjectDao.findLargeObject(createdLargeObject.getId());

        assertNotNull(foundLargeObject);
        assertEquals(largeObject.getPath(), foundLargeObject.getPath());
        assertEquals(largeObject.getUrl(), foundLargeObject.getUrl());
        assertEquals(largeObject.getMimeType(), foundLargeObject.getMimeType());
        assertEquals(largeObject.getAccessPermissions(), foundLargeObject.getAccessPermissions());
        assertTrue(foundDeleted.isEmpty());

        assertTrue(createdEventIds.contains(createdLargeObject.getId()),
                "Expected LARGE_OBJECT_CREATED event for " + createdLargeObject.getId());
        assertTrue(deletedEventIds.contains(createdLargeObject.getId()),
                "Expected LARGE_OBJECT_DELETED event for " + createdLargeObject.getId());

    }

    @Test
    public void testUpdateLargeObject() {
        final LargeObject largeObject = largeObjectTestFactory.createDefaultLargeObject(largeObjectTestFactory.wildcardAccess());
        LargeObject createdLargeObject = largeObjectDao.createLargeObject(largeObject);
        LargeObject largeObjectToUpdate = largeObjectDao.getLargeObject(createdLargeObject.getId());

        largeObjectToUpdate.setUrl("changedUrl");
        largeObjectToUpdate.setAccessPermissions(largeObjectTestFactory.notWildcardReadAccess());
        largeObjectDao.updateLargeObject(largeObjectToUpdate);

        LargeObject updatedLargeObject = largeObjectDao.getLargeObject(largeObjectToUpdate.getId());

        assertNotNull(updatedLargeObject);
        assertEquals(largeObject.getPath(), updatedLargeObject.getPath());
        assertEquals(updatedLargeObject.getUrl(), "changedUrl");
        assertEquals(largeObject.getMimeType(), updatedLargeObject.getMimeType());
        assertFalse(updatedLargeObject.getAccessPermissions().getRead().isWildcard());
        assertTrue(updatedLargeObject.getAccessPermissions().getWrite().isWildcard());

        assertTrue(updatedEventIds.contains(updatedLargeObject.getId()),
                "Expected LARGE_OBJECT_UPDATED event for " + updatedLargeObject.getId());
    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }

    public LargeObjectTestFactory getLargeObjectTestFactory() {
        return largeObjectTestFactory;
    }

    @Inject
    public void setLargeObjectTestFactory(LargeObjectTestFactory largeObjectTestFactory) {
        this.largeObjectTestFactory = largeObjectTestFactory;
    }
}
