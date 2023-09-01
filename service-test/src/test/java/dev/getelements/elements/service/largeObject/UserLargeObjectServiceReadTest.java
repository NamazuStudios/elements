package dev.getelements.elements.service.largeObject;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.largeobject.UserLargeObjectService;
import dev.getelements.elements.service.profile.UserProfileService;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeObject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

public class UserLargeObjectServiceReadTest extends LargeObjectServiceTestBase{

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Inject
    private UserProfileService userProfileService;

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
        when(userProfileService.getCurrentProfile()).then(a -> mock(Profile.class));

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToFindByPermittedUser() {
        User permittedUser = mock(User.class);
        userLargeObjectService.setUser(permittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        LargeObject largeObjectWithUserReadAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserReadAccess));
        when(userProfileService.getCurrentProfile()).then(a -> mock(Profile.class));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFindByNonPermittedUser() {
        User permittedUser = mock(User.class);
        User nonPermittedUser = mock(User.class);

        userLargeObjectService.setUser(nonPermittedUser);   //logged in user is not permitted
        List<User> permittedReadAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.findLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToFindByPermittedProfile() {
        Profile permittedProfile = mock(Profile.class);
        when(userProfileService.getCurrentProfile()).then(a -> permittedProfile);

        List<Profile> permittedReadAccess = asList(permittedProfile);
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        Optional<LargeObject> result = userLargeObjectService.findLargeObject(TEST_ID);

        assertTrue(result.isPresent());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToFindByNonPermittedProfile() {
        Profile permittedProfile = mock(Profile.class);
        Profile notPermittedProfile = mock(Profile.class);

        when(userProfileService.getCurrentProfile()).then(a -> notPermittedProfile);

        List<Profile> permittedReadAccess = asList(permittedProfile);
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
        when(userProfileService.getCurrentProfile()).then(a -> mock(Profile.class));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }

    @Test
    public void shouldAllowToReadContentByPermittedUser() throws IOException {
        User permittedUser = mock(User.class);
        userLargeObjectService.setUser(permittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));
        when(userProfileService.getCurrentProfile()).then(a -> mock(Profile.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContentByNonPermittedUser() throws IOException {
        User permittedUser = mock(User.class);
        User nonPermittedUser = mock(User.class);

        userLargeObjectService.setUser(nonPermittedUser);   //logged in user is not permitted
        List<User> permittedReadAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }

    @Test
    public void shouldAllowToReadContentByPermittedProfile() throws IOException {
        Profile permittedProfile = mock(Profile.class);
        when(userProfileService.getCurrentProfile()).then(a -> permittedProfile);

        List<Profile> permittedReadAccess = asList(permittedProfile);
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(largeObjectBucket.readObject(TEST_ID)).then(a -> mock(FileInputStream.class));

        InputStream inputStream = userLargeObjectService.readLargeObjectContent(TEST_ID);

        assertTrue(inputStream instanceof FileInputStream);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToReadContentByNonPermittedProfile() throws IOException {
        Profile permittedProfile = mock(Profile.class);
        Profile notPermittedProfile = mock(Profile.class);

        when(userProfileService.getCurrentProfile()).then(a -> notPermittedProfile);

        List<Profile> permittedReadAccess = asList(permittedProfile);
        LargeObject largeObject = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.readLargeObjectContent(TEST_ID);
    }
}
