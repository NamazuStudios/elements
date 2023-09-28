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

@Guice(modules = {LargeObjectServiceTestModule.class, UserProfileModule.class})
public class UserLargeObjectServiceDeleteTest extends LargeObjectServiceTestBase{

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Inject
    private User user;

    @Inject
    private Optional<Profile> profileOptional;

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

        List<User> permittedReadAccess = asList(user);
        List<User> permittedDeleteAccess = asList(user);
        LargeObject largeObjectWithUSerDeleteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUSerDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDeleteContentByNotPermittedUser() throws IOException {

        List<User> permittedReadAccess = asList(factory.notPermittedUser());
        List<User> permittedDeleteAccess = asList(factory.notPermittedUser());
        LargeObject largeObject = factory.largeObjectWithUsersAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObject));

        userLargeObjectService.deleteLargeObject(TEST_ID);
    }

    @Test
    public void shouldAllowToDeletePermittedProfile() throws IOException {

        List<Profile> permittedReadAccess = asList(profileOptional.get());
        List<Profile> permittedDeleteAccess = asList(profileOptional.get());
        LargeObject largeObjectWithProfileDeleteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);

        verify(largeObjectBucket, times(1)).deleteLargeObject(TEST_ID);
        verify(largeObjectDao, times(1)).deleteLargeObject(TEST_ID);
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToDeleteNotPermittedProfile() throws IOException {

        List<Profile> permittedReadAccess = asList(factory.notPermittedProfile());
        List<Profile> permittedDeleteAccess = asList(factory.notPermittedProfile());
        LargeObject largeObjectWithProfileDeleteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, emptyList(), permittedDeleteAccess);

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileDeleteAccess));

        userLargeObjectService.deleteLargeObject(TEST_ID);
    }
}
