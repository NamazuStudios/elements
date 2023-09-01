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
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeObject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class UserLargeObjectServiceWriteTest extends LargeObjectServiceTestBase {

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Inject
    private UserProfileService userProfileService;

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToCreate() {
        userLargeObjectService.createLargeObject(factory.createRequestWithFullAccess());
    }

    @Test
    public void shouldUpdate() {
        UpdateLargeObjectRequest request = factory.updateRequestWithFullAccess();
        request.setMimeType("changedMime");
        request.setDelete(factory.defaultRequestWithWildcardAccess(false));

        LargeObject largeObjectToUpdate = factory.wildcardLargeObject();
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectToUpdate));
        when(largeObjectDao.updateLargeObject(largeObjectToUpdate)).then(a -> a.getArgument(0));

        LargeObject result = userLargeObjectService.updateLargeObject(TEST_ID, request);

        assertNotNull(result);
        assertEquals(result.getMimeType(), "changedMime");
        assertFalse(result.getAccessPermissions().getDelete().isWildcard());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdate() {
        LargeObject largeObjectWithNoWriteAccess = factory.defaultLargeObjectWithAccess(true, false, true);
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithNoWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test
    public void shouldAllowToUpdatePermittedUser() {
        User permittedUser = mock(User.class);
        userLargeObjectService.setUser(permittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObjectWithUserWriteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserWriteAccess));
        when(largeObjectDao.updateLargeObject(any())).then(a -> a.getArgument(0));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdateNonPermittedUser() {
        User permittedUser = mock(User.class);
        User nonPermittedUser = mock(User.class);
        userLargeObjectService.setUser(nonPermittedUser);
        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObjectWithUserWriteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test
    public void shouldAllowToUpdatePermittedProfile() {
        Profile permittedProfile = mock(Profile.class);
        when(userProfileService.getCurrentProfile()).then(a -> permittedProfile);
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedWriteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileWriteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileWriteAccess));
        when(largeObjectDao.updateLargeObject(any())).then(a -> a.getArgument(0));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdateNonPermittedProfile() {
        Profile permittedProfile = mock(Profile.class);
        Profile nonPermittedProfile = mock(Profile.class);
        when(userProfileService.getCurrentProfile()).then(a -> nonPermittedProfile);
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedWriteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileWriteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }
}
