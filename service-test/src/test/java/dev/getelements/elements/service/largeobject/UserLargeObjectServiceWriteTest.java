package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;
import org.mockito.Mock;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = LargeObjectServiceTestModule.class)
public class UserLargeObjectServiceWriteTest extends LargeObjectServiceTestBase {

    @Mock
    @Inject
    private UserService userService;

    @Mock
    @Inject
    private ProfileService profileService;

    @Mock
    @Inject
    private LargeObjectDao largeObjectDao;

    @Mock
    @Inject
    private LargeObjectBucket largeObjectBucket;

    @Inject
    private UserLargeObjectService userLargeObjectService;

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
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdate() {
        LargeObject largeObjectWithNoWriteAccess = factory.defaultLargeObjectWithAccess(true, false, true);
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithNoWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test
    public void shouldAllowToUpdatePermittedUser() {
        User permittedUser = factory.permittedUser();
        when(userService.getCurrentUser()).thenReturn(permittedUser);

        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObjectWithUserWriteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserWriteAccess));
        when(largeObjectDao.updateLargeObject(any())).then(a -> a.getArgument(0));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdateNonPermittedUser() {
        User permittedUser = factory.permittedUser();

        List<User> permittedReadAccess = asList(permittedUser);
        List<User> permittedWriteAccess = asList(permittedUser);
        LargeObject largeObjectWithUserWriteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(userService.getCurrentUser()).thenReturn(factory.notPermittedUser());
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test
    public void shouldAllowToUpdatePermittedProfile() {
        Profile permittedProfile = factory.permittedProfile();
        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedWriteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileWriteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(profileService.getCurrentProfile()).thenReturn(permittedProfile);
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileWriteAccess));
        when(largeObjectDao.updateLargeObject(any())).then(a -> a.getArgument(0));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdateNonPermittedProfile() {
        Profile permittedProfile = factory.permittedProfile();

        List<Profile> permittedReadAccess = asList(permittedProfile);
        List<Profile> permittedWriteAccess = asList(permittedProfile);
        LargeObject largeObjectWithProfileWriteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(profileService.getCurrentProfile()).thenReturn(factory.notPermittedProfile());
        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }
}
