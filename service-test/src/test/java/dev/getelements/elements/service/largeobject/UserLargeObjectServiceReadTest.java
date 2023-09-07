package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
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

@Guice(modules = LargeObjectServiceTestModule.class)
public class UserLargeObjectServiceReadTest extends LargeObjectServiceTestBase{

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Mock
    @Inject
    private UserService userService;

    @Mock
    @Inject
    private ProfileService profileService;

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
        when(profileService.getCurrentProfile()).then(a -> factory.notPermittedProfile());

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToFindByPermittedUser() {
        User permittedUser = factory.permittedUser();
        when(userService.getCurrentUser()).thenReturn(permittedUser);

        List<User> permittedReadAccess = asList(permittedUser);
        LargeObject largeObjectWithUserReadAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserReadAccess));
        when(profileService.getCurrentProfile()).then(a -> mock(Profile.class));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFindByNonPermittedUser() {

        List<User> permittedReadAccess = asList(factory.permittedUser());
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(userService.getCurrentUser()).thenReturn(factory.notPermittedUser());
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToFindByPermittedProfile() {
        Profile permittedProfile = factory.permittedProfile();
        List<Profile> permittedReadAccess = asList(permittedProfile);
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(profileService.getCurrentProfile()).thenReturn(permittedProfile);
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFindByNonPermittedProfile() {
        List<Profile> permittedReadAccess = asList(factory.permittedProfile());
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(profileService.findCurrentProfile()).then(a -> Optional.of(factory.notPermittedProfile()));
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
        when(profileService.getCurrentProfile()).then(a -> factory.permittedProfile());

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }

    @Test
    public void shouldAllowToReadContentByPermittedUser() throws IOException {
        User permittedUser = factory.permittedUser();
        when(userService.getCurrentUser()).thenReturn(permittedUser);

        List<User> permittedReadAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));
        when(profileService.getCurrentProfile()).then(a -> mock(Profile.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContentByNonPermittedUser() throws IOException {

        List<User> permittedReadAccess = asList(factory.permittedUser());
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(userService.getCurrentUser()).thenReturn(factory.notPermittedUser());
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }

    @Test
    public void shouldAllowToReadContentByPermittedProfile() throws IOException {
        Profile permittedProfile = factory.permittedProfile();
        when(profileService.getCurrentProfile()).then(a -> permittedProfile);

        List<Profile> permittedReadAccess = asList(permittedProfile);
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContentByNonPermittedProfile() throws IOException {

        List<Profile> permittedReadAccess = asList(factory.permittedProfile());
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(profileService.getCurrentProfile()).then(a -> factory.notPermittedProfile());
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }
}
