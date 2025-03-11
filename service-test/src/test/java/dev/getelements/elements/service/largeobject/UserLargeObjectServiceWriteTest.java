package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import org.mockito.Mock;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.service.largeobject.LargeObjectServiceTestFactory.TEST_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = {LargeObjectServiceTestModule.class, UserProfileModule.class})
public class UserLargeObjectServiceWriteTest extends LargeObjectServiceTestBase {

    @Mock
    @Inject
    private LargeObjectDao largeObjectDao;

    @Mock
    @Inject
    private LargeObjectBucket largeObjectBucket;

    @Inject
    private UserLargeObjectService userLargeObjectService;

    @Inject
    private User user;

    @Inject
    private Optional<Profile> profileOptional;

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

        List<User> permittedReadAccess = asList(user);
        List<User> permittedWriteAccess = asList(user);
        LargeObject largeObjectWithUserWriteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserWriteAccess));
        when(largeObjectDao.updateLargeObject(any())).then(a -> a.getArgument(0));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdateNonPermittedUser() {

        List<User> permittedReadAccess = asList(factory.notPermittedUser());
        List<User> permittedWriteAccess = asList(factory.notPermittedUser());
        LargeObject largeObjectWithUserWriteAccess = factory.largeObjectWithUsersAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithUserWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test
    public void shouldAllowToUpdatePermittedProfile() {

        List<Profile> permittedReadAccess = asList(profileOptional.get());
        List<Profile> permittedWriteAccess = asList(profileOptional.get());
        LargeObject largeObjectWithProfileWriteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileWriteAccess));
        when(largeObjectDao.updateLargeObject(any())).then(a -> a.getArgument(0));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }

    @Test(expectedExceptions = {ForbiddenException.class})
    public void shouldNotAllowToUpdateNonPermittedProfile() {

        List<Profile> permittedReadAccess = asList(factory.notPermittedProfile());
        List<Profile> permittedWriteAccess = asList(factory.notPermittedProfile());
        LargeObject largeObjectWithProfileWriteAccess = factory.largeObjectWithProfilesAccess(permittedReadAccess, permittedWriteAccess, emptyList());

        when(largeObjectDao.findLargeObject(TEST_ID)).then(a -> Optional.of(largeObjectWithProfileWriteAccess));

        userLargeObjectService.updateLargeObject(TEST_ID, factory.updateRequestWithFullAccess());
    }
}
