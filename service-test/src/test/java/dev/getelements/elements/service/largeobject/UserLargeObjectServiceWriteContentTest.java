package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import jnr.ffi.annotations.In;
import org.mockito.Mock;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.*;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

@Guice(modules = LargeObjectServiceTestModule.class)
public class UserLargeObjectServiceWriteContentTest extends LargeObjectServiceTestBase {

    @Inject
    private UserService userService;

    @Inject
    private ProfileService profileService;

    @Inject
    private UserLargeObjectService userLargeObjectService;

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
        when(userService.getCurrentUser()).thenReturn(permittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));
        when(profileService.getCurrentProfile()).then(a -> mock(Profile.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID);

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToWriteContentByNonPermittedUser() throws IOException {

        User permittedUser = mock(User.class);
        User nonPermittedUser = mock(User.class);
        when(userService.getCurrentUser()).thenReturn(nonPermittedUser);

        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }


}
