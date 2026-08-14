package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.profile.CreateProfileRequest;
import dev.getelements.elements.sdk.model.profile.UpdateProfileRequest;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;

/**
 * Covers the (Elements 3.9+) Application#displayNameRegex enforcement added to profile create/update.
 */
public class ProfileServiceUtilsTest {

    private static final String APPLICATION_ID = "app-1";
    private static final String USER_ID = "user-1";
    private static final String PROFILE_ID = "profile-1";

    private ProfileServiceUtils profileServiceUtils;
    private ApplicationDao applicationDao;
    private UserDao userDao;

    @BeforeMethod
    public void setup() {
        profileServiceUtils = new ProfileServiceUtils();
        applicationDao = mock(ApplicationDao.class);
        userDao = mock(UserDao.class);
        profileServiceUtils.setApplicationDao(applicationDao);
        profileServiceUtils.setUserDao(userDao);

        when(userDao.getUser(anyString())).thenReturn(userWithId(USER_ID));
    }

    @Test
    public void testCreateAllowsDisplayNameMatchingRegex() {
        when(applicationDao.getActiveApplication(APPLICATION_ID))
                .thenReturn(applicationWithRegex(APPLICATION_ID, "^[a-z]+$"));

        final var profile = profileServiceUtils.getProfileForCreate(createRequest("validname"));
        assertEquals(profile.getDisplayName(), "validname");
    }

    @Test
    public void testCreateRejectsDisplayNameNotMatchingRegex() {
        when(applicationDao.getActiveApplication(APPLICATION_ID))
                .thenReturn(applicationWithRegex(APPLICATION_ID, "^[a-z]+$"));

        expectThrows(InvalidDataException.class,
                () -> profileServiceUtils.getProfileForCreate(createRequest("Invalid Name 123")));
    }

    @Test
    public void testCreateSkipsCheckWhenRegexBlank() {
        when(applicationDao.getActiveApplication(APPLICATION_ID))
                .thenReturn(applicationWithRegex(APPLICATION_ID, null));

        final var profile = profileServiceUtils.getProfileForCreate(createRequest("Anything At All 123"));
        assertEquals(profile.getDisplayName(), "Anything At All 123");
    }

    @Test
    public void testCreateThrowsInternalExceptionWhenRegexDoesNotCompile() {
        when(applicationDao.getActiveApplication(APPLICATION_ID))
                .thenReturn(applicationWithRegex(APPLICATION_ID, "[unterminated"));

        expectThrows(InternalException.class,
                () -> profileServiceUtils.getProfileForCreate(createRequest("anything")));
    }

    @Test
    public void testUpdateRejectsDisplayNameNotMatchingRegex() {
        when(applicationDao.getApplication(APPLICATION_ID))
                .thenReturn(applicationWithRegex(APPLICATION_ID, "^[a-z]+$"));

        final var updateRequest = new UpdateProfileRequest();
        updateRequest.setDisplayName("Not Valid");

        expectThrows(InvalidDataException.class,
                () -> profileServiceUtils.getProfileForUpdate(PROFILE_ID, updateRequest, APPLICATION_ID));
    }

    @Test
    public void testUpdateSkipsCheckWhenDisplayNameNotBeingChanged() {
        final var updateRequest = new UpdateProfileRequest();
        updateRequest.setMetadata(null);

        final var updates = profileServiceUtils.getProfileForUpdate(PROFILE_ID, updateRequest, APPLICATION_ID);
        assertEquals(updates.getId(), PROFILE_ID);
    }

    // ---------- helpers ----------

    private static CreateProfileRequest createRequest(final String displayName) {
        final var request = new CreateProfileRequest();
        request.setUserId(USER_ID);
        request.setApplicationId(APPLICATION_ID);
        request.setDisplayName(displayName);
        return request;
    }

    private static Application applicationWithRegex(final String id, final String regex) {
        final var application = new Application();
        application.setId(id);
        application.setDisplayNameRegex(regex);
        return application;
    }

    private static User userWithId(final String id) {
        final var user = new User();
        user.setId(id);
        return user;
    }

}
