package dev.getelements.elements.service.user;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.dao.UserUidDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserUid;
import jakarta.inject.Inject;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.function.Function;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.sdk.dao.UserUidDao.SCHEME_NAME;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class UsernamePasswordLinkServiceTest {

    private static final String CURRENT_USER_ID = "current-user-id";
    private static final String USERNAME        = "alice";
    private static final String PASSWORD        = "s3cr3t";

    @Inject private UserUsernamePasswordLinkService service;
    @Inject private UserDao userDao;
    @Inject private UserUidDao userUidDao;
    @Inject private Transaction transaction;

    private User currentUser;

    @BeforeMethod
    public void setup() {
        currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        createInjector(new TestModule(currentUser)).injectMembers(this);
    }

    /** Case A: account has no name — username claimed and name+password set inside a transaction. */
    @Test
    public void linkUsernamePassword_noExistingName_setsNameAndPassword() {
        currentUser.setName(null);
        final var expected = new User();
        expected.setId(CURRENT_USER_ID);
        expected.setName(USERNAME);
        when(transaction.getDao(UserUidDao.class)).thenReturn(userUidDao);
        when(transaction.getDao(UserDao.class)).thenReturn(userDao);
        when(userDao.updateUser(any(User.class), eq(PASSWORD))).thenReturn(expected);

        final var result = service.linkUsernamePassword(USERNAME, PASSWORD);

        assertEquals(result.getName(), USERNAME);
        final var uidCaptor = ArgumentCaptor.forClass(UserUid.class);
        verify(userUidDao).createUserUidStrict(uidCaptor.capture());
        final var uid = uidCaptor.getValue();
        assertEquals(uid.getId(), USERNAME);
        assertEquals(uid.getScheme(), SCHEME_NAME);
        assertEquals(uid.getUserId(), CURRENT_USER_ID);

        final var userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDao).updateUser(userCaptor.capture(), eq(PASSWORD));
        assertEquals(userCaptor.getValue().getName(), USERNAME);
    }

    /** Case A: username already taken — DuplicateException from UID creation is re-thrown as ForbiddenException. */
    @Test(expectedExceptions = ForbiddenException.class)
    public void linkUsernamePassword_noExistingName_usernameTaken_throwsForbidden() {
        currentUser.setName(null);
        when(transaction.getDao(UserUidDao.class)).thenReturn(userUidDao);
        when(transaction.getDao(UserDao.class)).thenReturn(userDao);
        doThrow(new DuplicateException("taken")).when(userUidDao).createUserUidStrict(any());

        service.linkUsernamePassword(USERNAME, PASSWORD);
    }

    /** Case B: account already has matching name — just set password; no transaction needed. */
    @Test
    public void linkUsernamePassword_existingNameMatches_setsPassword() {
        currentUser.setName(USERNAME);
        final var expected = new User();
        expected.setId(CURRENT_USER_ID);
        when(userDao.setPassword(CURRENT_USER_ID, PASSWORD)).thenReturn(expected);

        service.linkUsernamePassword(USERNAME, PASSWORD);

        verify(userDao).setPassword(CURRENT_USER_ID, PASSWORD);
        verify(transaction, never()).getDao(any());
    }

    /** Case C: account has a different name → ForbiddenException; no DAO calls. */
    @Test(expectedExceptions = ForbiddenException.class)
    public void linkUsernamePassword_existingNameMismatch_throwsForbidden() {
        currentUser.setName("bob");

        service.linkUsernamePassword(USERNAME, PASSWORD);

        verify(userDao, never()).setPassword(any(), any());
        verify(userDao, never()).updateUser(any(User.class), any());
    }

    /** Leading/trailing whitespace on the supplied username is trimmed before use. */
    @Test
    public void linkUsernamePassword_usernameNormalized() {
        currentUser.setName(USERNAME);
        when(userDao.setPassword(CURRENT_USER_ID, PASSWORD)).thenReturn(new User());

        service.linkUsernamePassword("  " + USERNAME + "  ", PASSWORD);

        verify(userDao).setPassword(CURRENT_USER_ID, PASSWORD);
    }

    private static class TestModule extends AbstractModule {

        private final User currentUser;

        TestModule(User currentUser) {
            this.currentUser = currentUser;
        }

        @Override
        protected void configure() {
            final var tx = mock(Transaction.class);
            // Make performAndClose actually invoke the lambda so the transaction body executes.
            doAnswer(invocation -> {
                Function<Transaction, Object> fn = invocation.getArgument(0);
                return fn.apply(tx);
            }).when(tx).performAndClose(any());

            bind(User.class).toInstance(currentUser);
            bind(UserDao.class).toInstance(mock(UserDao.class));
            bind(UserUidDao.class).toInstance(mock(UserUidDao.class));
            // Binding Transaction also satisfies Provider<Transaction> injection automatically.
            bind(Transaction.class).toInstance(tx);
        }
    }

}
