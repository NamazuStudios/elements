package dev.getelements.elements.service.user;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.user.VerificationStatus;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_EMAIL;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class EmailPasswordLinkServiceTest {

    private static final String CURRENT_USER_ID = "current-user-id";
    private static final String OTHER_USER_ID   = "other-user-id";
    private static final String EMAIL           = "test@example.com";
    private static final String PASSWORD        = "s3cr3t";

    @Inject private UserEmailPasswordLinkService service;
    @Inject private UserDao userDao;
    @Inject private UserUidDao userUidDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    public void linkEmailPassword_verifiedEmail_setsPassword() {
        final var uid = uidFor(EMAIL, CURRENT_USER_ID, VerificationStatus.VERIFIED);
        when(userUidDao.getUserUid(EMAIL, SCHEME_EMAIL)).thenReturn(uid);

        final var expected = new User();
        expected.setId(CURRENT_USER_ID);
        when(userDao.setPassword(CURRENT_USER_ID, PASSWORD)).thenReturn(expected);

        final var result = service.linkEmailPassword(EMAIL, PASSWORD);

        assertEquals(result.getId(), CURRENT_USER_ID);
        verify(userDao).setPassword(CURRENT_USER_ID, PASSWORD);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void linkEmailPassword_unverifiedEmail_throwsForbidden() {
        final var uid = uidFor(EMAIL, CURRENT_USER_ID, VerificationStatus.PENDING);
        when(userUidDao.getUserUid(EMAIL, SCHEME_EMAIL)).thenReturn(uid);

        service.linkEmailPassword(EMAIL, PASSWORD);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void linkEmailPassword_otherUsersEmail_throwsForbidden() {
        final var uid = uidFor(EMAIL, OTHER_USER_ID, VerificationStatus.VERIFIED);
        when(userUidDao.getUserUid(EMAIL, SCHEME_EMAIL)).thenReturn(uid);

        service.linkEmailPassword(EMAIL, PASSWORD);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void linkEmailPassword_unknownEmail_throwsNotFound() {
        when(userUidDao.getUserUid(EMAIL, SCHEME_EMAIL)).thenThrow(new NotFoundException("not found"));

        service.linkEmailPassword(EMAIL, PASSWORD);
    }

    @Test
    public void linkEmailPassword_emailNormalized() {
        when(userUidDao.getUserUid(EMAIL, SCHEME_EMAIL))
                .thenReturn(uidFor(EMAIL, CURRENT_USER_ID, VerificationStatus.VERIFIED));
        when(userDao.setPassword(CURRENT_USER_ID, PASSWORD)).thenReturn(new User());

        service.linkEmailPassword("  TEST@EXAMPLE.COM  ", PASSWORD);

        verify(userUidDao).getUserUid(EMAIL, SCHEME_EMAIL);
    }

    private static UserUid uidFor(final String id, final String userId, final VerificationStatus status) {
        final var uid = new UserUid();
        uid.setId(id);
        uid.setScheme(SCHEME_EMAIL);
        uid.setUserId(userId);
        uid.setVerificationStatus(status);
        return uid;
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            final var currentUser = new User();
            currentUser.setId(CURRENT_USER_ID);
            bind(User.class).toInstance(currentUser);
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
        }
    }

}
