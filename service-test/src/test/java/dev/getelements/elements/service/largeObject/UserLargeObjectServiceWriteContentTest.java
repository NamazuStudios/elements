package dev.getelements.elements.service.largeObject;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.largeobject.UserLargeObjectService;
import dev.getelements.elements.service.profile.UserProfileService;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.*;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeObject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class UserLargeObjectServiceWriteContentTest extends LargeObjectServiceTestBase {

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Inject
    private UserProfileService userProfileService;

    @Test
    public void shouldWriteContent() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID);

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test
    public void shouldNotAllowToWriteContent() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID);

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test
    public void shouldAllowToWriteContentByPermittedUser() throws IOException {
        User permittedUser = mock(User.class);
        userLargeObjectService.setUser(permittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));
        when(userProfileService.getCurrentProfile()).then(a -> mock(Profile.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID);

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToWriteContentByNonPermittedUser() throws IOException {
        User permittedUser = mock(User.class);
        User nonPermittedUser = mock(User.class);

        userLargeObjectService.setUser(nonPermittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }


}
