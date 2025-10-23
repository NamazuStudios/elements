package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.*;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Guice(modules = LargeObjectServiceTestModule.class)
public class SuperUserLargeObjectServiceTest extends LargeObjectServiceTestBase {

    @Inject
    private SuperUserLargeObjectService superUserLargeObjectService;

    @Inject
    private UserDao userDao;

    @Inject
    private ProfileDao profileDao;

    @Test
    public void shouldCreate() {
        CreateLargeObjectRequest request = factory.createRequestWithFullAccess();
        when(largeObjectDao.createLargeObject(any())).then(a -> {
            LargeObject objectToSave = a.getArgument(0);
            assertEquals(objectToSave.getMimeType(), request.getMimeType());
            AccessPermissions permissionSet = objectToSave.getAccessPermissions();
            assertTrue(permissionSet.getRead().isWildcard());
            assertTrue(permissionSet.getWrite().isWildcard());
            assertTrue(permissionSet.getDelete().isWildcard());

            return objectToSave;
        });

        LargeObject result = superUserLargeObjectService.createLargeObject(request);

        assertNotNull(result);
    }

    @Test
    public void shouldCreateWithMixedPermissions() {
        CreateLargeObjectRequest request = factory.defaultCreateRequestWithWildcardAccess(false, false, false);
        List<String> readAllowedUserId = asList("u1", "u2");
        List<String> writeAllowedProfileId = asList("p1");

        request.setRead(factory.subjectRequestWithUsersAndProfiles(readAllowedUserId, emptyList()));
        request.setWrite(factory.subjectRequestWithUsersAndProfiles(emptyList(), writeAllowedProfileId));

        when(userDao.getUser("u1")).then(a -> mock(User.class));
        when(userDao.getUser("u2")).then(a -> mock(User.class));
        when(profileDao.getActiveProfile("p1")).then(a -> mock(Profile.class));

        when(largeObjectDao.createLargeObject(any())).then(a -> {
            LargeObject objectToSave = a.getArgument(0);
            assertEquals(objectToSave.getMimeType(), request.getMimeType());
            AccessPermissions permissionSet = objectToSave.getAccessPermissions();

            assertFalse(permissionSet.getRead().isWildcard());
            assertEquals(permissionSet.getRead().getUsers().size(), 2);
            assertTrue(permissionSet.getRead().getProfiles().isEmpty());
            assertFalse(permissionSet.getWrite().isWildcard());
            assertTrue(permissionSet.getWrite().getUsers().isEmpty());
            assertEquals(permissionSet.getWrite().getProfiles().size(), 1);
            assertFalse(permissionSet.getDelete().isWildcard());

            return objectToSave;
        });

        LargeObject result = superUserLargeObjectService.createLargeObject(request);

        assertNotNull(result);
    }

    @Test
    public void shouldUpdate() {
        UpdateLargeObjectRequest request = factory.updateRequestWithFullAccess();
        request.setMimeType("changedMime");
        request.setDelete(factory.defaultRequestWithWildcardAccess(false));

        LargeObject largeObjectToUpdate = factory.wildcardLargeObject();
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectToUpdate));
        when(largeObjectDao.updateLargeObject(largeObjectToUpdate)).then(a -> a.getArgument(0));

        LargeObject result = superUserLargeObjectService.updateLargeObject(TEST_ID, request);

        assertNotNull(result);
        assertEquals(result.getMimeType(), "changedMime");
        assertFalse(result.getAccessPermissions().getDelete().isWildcard());
    }

    @Test
    public void shouldDelete() throws IOException {
        LargeObject object = factory.defaultLargeObject();
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(object));

        superUserLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test
    public void shouldReadLargeObjectContent() throws IOException {
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream result = superUserLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(result instanceof FileInputStream);
    }

    @Test
    public void shouldWriteLargeObjectContent() throws IOException {
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));

        OutputStream result = superUserLargeObjectService.writeLargeObjectContent(TEST_ID, "shouldWriteLargeObjectContent");

        assertTrue(result instanceof FileOutputStream);
    }
}
