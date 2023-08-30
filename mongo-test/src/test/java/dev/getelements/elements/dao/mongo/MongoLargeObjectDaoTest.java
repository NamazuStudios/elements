package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.model.largeobject.LargeObject;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Optional;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoLargeObjectDaoTest {

    private LargeObjectTestFactory largeObjectTestFactory;
    private LargeObjectDao largeObjectDao;

    @Test
    public void testCreateGetAndDeleteLargeObject() {
        final LargeObject largeObject = largeObjectTestFactory.createDefaultLargeObject(largeObjectTestFactory.wildcardAccess());

        LargeObject createdLargeObject = largeObjectDao.createLargeObject(largeObject);
        LargeObject foundLargeObject = largeObjectDao.getLargeObject(createdLargeObject.getId());
        LargeObject deletedLargeObject = largeObjectDao.deleteLargeObject(createdLargeObject.getId());
        Optional<LargeObject> foundDeleted = largeObjectDao.findLargeObject(deletedLargeObject.getId());

        assertNotNull(foundLargeObject);
        assertEquals(largeObject.getPath(), foundLargeObject.getPath());
        assertEquals(largeObject.getUrl(), foundLargeObject.getUrl());
        assertEquals(largeObject.getMimeType(), foundLargeObject.getMimeType());
        assertEquals(largeObject.getAccessPermissions(), foundLargeObject.getAccessPermissions());
        assertTrue(foundDeleted.isEmpty());
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
