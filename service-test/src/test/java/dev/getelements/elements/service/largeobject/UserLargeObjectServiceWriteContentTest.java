package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

@Guice(modules = {LargeObjectServiceTestModule.class, UserProfileModule.class})
public class UserLargeObjectServiceWriteContentTest extends LargeObjectServiceTestBase {

    @Inject
    private User user;

    @Inject
    private Optional<Profile> profileOptional;

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Test
    public void shouldWriteContent() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID, "shouldWriteContent");

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test
    public void shouldNotAllowToWriteContent() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID, "shouldNotAllowToWriteContent");

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test
    public void shouldAllowToWriteContentByPermittedUser() throws IOException {

        List<User> permittedReadAccess = asList(user);
        List<User> permittedWriteAccess = asList(user);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.writeObject(TEST_ID)).then(a -> mock(FileOutputStream.class));

        OutputStream outputStream = userLargeObjectService.writeLargeObjectContent(TEST_ID, "shouldNotAllowToWriteContentByNonPermittedUser");

        assertTrue(outputStream instanceof FileOutputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToWriteContentByNonPermittedUser() throws IOException {


        List<User> permittedReadAccess = asList(factory.notPermittedUser());
        List<User> permittedWriteAccess = asList(factory.notPermittedUser());
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }


}
