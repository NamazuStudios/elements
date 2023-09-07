package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import org.mockito.Mock;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.*;

@Guice(modules = LargeObjectServiceTestModule.class)
public class UserLargeObjectServiceDeleteTest extends LargeObjectServiceTestBase{

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Mock
    @Inject
    private ProfileService profileService;

    @Mock
    @Inject
    private UserService userService;

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
        User permittedUser = factory.permittedUser();

        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedDeleteAccess = asList(permittedUser);
        LargeObject largeObjectWithUSerDeleteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUSerDeleteAccess));
        when(userService.getCurrentUser()).thenReturn(permittedUser);
        when(profileService.findCurrentProfile()).then(a -> Optional.of(factory.permittedProfile()));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDeleteContentByNotPermittedUser() throws IOException {
        User permittedUser = factory.permittedUser();

        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedDeleteAccess = asList(permittedUser);
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));
        when(userService.getCurrentUser()).thenReturn(factory.notPermittedUser());

        userLargeObjectService.deleteLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToDeletePermittedProfile() throws IOException {
        Profile permittedProfile = factory.permittedProfile();
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedDeleteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileDeleteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(profileService.getCurrentProfile()).then(a -> permittedProfile);
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDeleteNotPermittedProfile() throws IOException {
        Profile permittedProfile = factory.permittedProfile();
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedDeleteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileDeleteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(profileService.findCurrentProfile()).thenReturn(Optional.of(factory.notPermittedProfile()));
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);
    }
}
