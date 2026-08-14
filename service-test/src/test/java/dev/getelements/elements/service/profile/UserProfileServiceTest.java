package dev.getelements.elements.service.profile;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.sdk.model.user.User;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.expectThrows;

/**
 * Covers the (Elements 3.9+) Application#authoritativeProfilePicture enforcement on the self-service
 * profile-image update path.
 */
public class UserProfileServiceTest {

    private static final String USER_ID = "user-1";
    private static final String PROFILE_ID = "profile-1";
    private static final String APPLICATION_ID = "app-1";

    private UserProfileService userProfileService;
    private ProfileDao profileDao;
    private ApplicationDao applicationDao;
    private LargeObjectDao largeObjectDao;
    private ProfileImageObjectUtils profileImageObjectUtils;
    private ProfileServiceUtils profileServiceUtils;

    @BeforeMethod
    public void setup() {

        profileDao = mock(ProfileDao.class);
        applicationDao = mock(ApplicationDao.class);
        largeObjectDao = mock(LargeObjectDao.class);
        profileImageObjectUtils = mock(ProfileImageObjectUtils.class);
        profileServiceUtils = mock(ProfileServiceUtils.class);

        when(profileServiceUtils.assignCdnUrl(any())).thenAnswer(inv -> inv.getArgument(0));

        userProfileService = new UserProfileService();
        userProfileService.setUser(userWithId(USER_ID));
        userProfileService.setProfileDao(profileDao);
        userProfileService.setApplicationDao(applicationDao);
        userProfileService.setLargeObjectDao(largeObjectDao);
        userProfileService.setProfileImageObjectUtils(profileImageObjectUtils);
        userProfileService.setProfileServiceUtils(profileServiceUtils);

        final var currentProfile = profileFor(PROFILE_ID, USER_ID, APPLICATION_ID);
        userProfileService.setCurrentProfileSupplier(() -> currentProfile);

        when(profileDao.getActiveProfile(PROFILE_ID)).thenReturn(profileFor(PROFILE_ID, USER_ID, APPLICATION_ID));
    }

    @Test
    public void testUpdateProfileImageRejectedWhenApplicationIsAuthoritative() throws Exception {

        when(applicationDao.getApplication(APPLICATION_ID))
                .thenReturn(applicationWithAuthoritativePicture(true));

        expectThrows(InvalidDataException.class, () ->
                userProfileService.updateProfileImage(PROFILE_ID, new UpdateProfileImageRequest()));

        verify(largeObjectDao, never()).createLargeObject(any());
        verify(largeObjectDao, never()).updateLargeObject(any());
    }

    @Test
    public void testUpdateProfileImageAllowedWhenApplicationIsNotAuthoritative() throws Exception {

        when(applicationDao.getApplication(APPLICATION_ID))
                .thenReturn(applicationWithAuthoritativePicture(false));

        final var existingImage = new LargeObjectReference();
        existingImage.setId("large-object-1");

        final var currentProfileWithImage = profileFor(PROFILE_ID, USER_ID, APPLICATION_ID);
        currentProfileWithImage.setImageObject(existingImage);
        userProfileService.setCurrentProfileSupplier(() -> currentProfileWithImage);

        when(largeObjectDao.getLargeObject("large-object-1")).thenReturn(new LargeObject());
        when(profileImageObjectUtils.updateProfileImageObject(any(), any(), any())).thenReturn(new LargeObject());
        when(profileDao.updateActiveProfile(any())).thenAnswer(inv -> inv.getArgument(0));

        final var request = new UpdateProfileImageRequest();
        request.setMimeType("image/png");

        final var updated = userProfileService.updateProfileImage(PROFILE_ID, request);
        assertNotNull(updated);

        verify(largeObjectDao).updateLargeObject(any());
    }

    // ---------- helpers ----------

    private static User userWithId(final String id) {
        final var user = new User();
        user.setId(id);
        return user;
    }

    private static Profile profileFor(final String profileId, final String userId, final String applicationId) {
        final var profile = new Profile();
        profile.setId(profileId);
        profile.setUser(userWithId(userId));
        final var application = new Application();
        application.setId(applicationId);
        profile.setApplication(application);
        return profile;
    }

    private static Application applicationWithAuthoritativePicture(final boolean authoritative) {
        final var application = new Application();
        application.setId(APPLICATION_ID);
        application.setAuthoritativeProfilePicture(authoritative);
        return application;
    }

}
