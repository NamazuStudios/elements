package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertTrue;

@Guice(modules = {LargeObjectServiceTestModule.class, UserProfileModule.class})
public class UserLargeObjectServiceReadTest extends LargeObjectServiceTestBase{

    @Inject
    private User user;

    @Inject
    private Optional<Profile> profileOptional;

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @BeforeMethod
    public void resetMocks() {
        reset(largeObjectDao);
    }

    @Test
    public void shouldFind() {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFind() {
        LargeObject largeObjectWithNoReadAccess = factory.defaultLargeObjectWithAccess(false, true, true);
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithNoReadAccess));

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToFindByPermittedUser() {

        List<User> permittedReadAccess = asList(user);
        LargeObject largeObjectWithUserReadAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserReadAccess));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFindByNonPermittedUser() {

        List<User> permittedReadAccess = asList(factory.notPermittedUser());
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToFindByPermittedProfile() {

        List<Profile> permittedReadAccess = asList(profileOptional.get());
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFindByNonPermittedProfile() {
        List<Profile> permittedReadAccess = asList(factory.notPermittedProfile());
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldReadContent() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContent() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.defaultLargeObjectWithAccess(false, true, true)));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }

    @Test
    public void shouldAllowToReadContentByPermittedUser() throws IOException {

        List<User> permittedReadAccess = asList(user);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContentByNonPermittedUser() throws IOException {

        List<User> permittedReadAccess = asList(factory.notPermittedUser());
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }

    @Test
    public void shouldAllowToReadContentByPermittedProfile() throws IOException {

        List<Profile> permittedReadAccess = asList(profileOptional.get());
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContentByNonPermittedProfile() throws IOException {

        List<Profile> permittedReadAccess = asList(factory.notPermittedProfile());
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }
}
