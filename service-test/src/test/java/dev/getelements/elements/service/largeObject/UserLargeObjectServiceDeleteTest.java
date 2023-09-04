package dev.getelements.elements.service.largeObject;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
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
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UserLargeObjectServiceDeleteTest extends LargeObjectServiceTestBase{

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Inject
    private UserProfileService userProfileService;

    @Test
    public void shouldDelete() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.wildcardLargeObject()));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDelete() throws IOException {
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(factory.defaultLargeObjectWithAccess(true, true, false)));
        userLargeObjectService.deleteLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToDeleteContentByPermittedUser() throws IOException {
        User permittedUser = mock(User.class);
        userLargeObjectService.setUser(permittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedDeleteAccess = asList(permittedUser);
        LargeObject largeObjectWithUSerDeleteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUSerDeleteAccess));
        when(userProfileService.getCurrentProfile()).then(a -> mock(Profile.class));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDeleteContentByNotPermittedUser() throws IOException {
        User permittedUser = mock(User.class);
        User notPermittedUser = mock(User.class);

        userLargeObjectService.setUser(notPermittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedDeleteAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.deleteLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToDeletePermittedProfile() throws IOException {
        Profile permittedProfile = mock(Profile.class);
        when(userProfileService.getCurrentProfile()).then(a -> permittedProfile);
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedDeleteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileDeleteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDeleteNotPermittedProfile() throws IOException {
        Profile permittedProfile = mock(Profile.class);
        Profile notPermittedProfile = mock(Profile.class);
        when(userProfileService.getCurrentProfile()).then(a -> notPermittedProfile);
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedDeleteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileDeleteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);
    }
}
