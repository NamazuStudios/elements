package dev.getelements.elements.service;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.model.auth.TotpUserState;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.service.auth.totp.UserTotpEnrollmentService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class UserTotpEnrollmentServiceTest {

    private static final String USER_ID = "user-1";

    private TotpConfigurationDao totpConfigurationDao;

    private TotpDao totpDao;

    private UserTotpEnrollmentService service;

    @BeforeMethod
    public void setup() {

        totpConfigurationDao = mock(TotpConfigurationDao.class);
        totpDao = mock(TotpDao.class);

        // Level is USER (not SUPERUSER) throughout this test class -- TOTP enrollment is a personal account
        // security feature available to any authenticated account, not just admins.
        final var user = new User();
        user.setId(USER_ID);
        user.setName("someuser");
        user.setLevel(User.Level.USER);

        service = new UserTotpEnrollmentService();
        service.setUser(user);
        service.setTotpConfigurationDao(totpConfigurationDao);
        service.setTotpDao(totpDao);

        final var enabledConfig = new TotpConfiguration();
        enabledConfig.setEnabled(true);
        when(totpConfigurationDao.getConfiguration()).thenReturn(enabledConfig);

    }

    @Test
    public void testBeginEnrollmentFailsWhenSystemDisabled() {

        final var disabledConfig = new TotpConfiguration();
        disabledConfig.setEnabled(false);
        when(totpConfigurationDao.getConfiguration()).thenReturn(disabledConfig);

        assertThrows(ForbiddenException.class, () -> service.beginEnrollment());
        verify(totpDao, never()).beginEnrollment(any(), any());

    }

    @Test
    public void testBeginEnrollmentStoresSecretAndReturnsProvisioningUri() {

        final var enrollment = service.beginEnrollment();

        assertNotNull(enrollment.getSecret());
        assertTrue(enrollment.getOtpAuthUri().startsWith("otpauth://totp/"));
        assertTrue(enrollment.getOtpAuthUri().contains("secret=" + enrollment.getSecret()));

        verify(totpDao).beginEnrollment(eq(USER_ID), eq(enrollment.getSecret()));

    }

    @Test
    public void testConfirmEnrollmentFailsWithoutPendingEnrollment() {
        when(totpDao.findState(USER_ID)).thenReturn(Optional.empty());
        assertThrows(InvalidDataException.class, () -> service.confirmEnrollment("123456"));
    }

    @Test
    public void testConfirmEnrollmentRejectsInvalidCode() {

        final var state = new TotpUserState();
        state.setUserId(USER_ID);
        state.setSecret("JBSWY3DPEHPK3PXP");
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(state));

        assertThrows(ForbiddenException.class, () -> service.confirmEnrollment("000000"));
        verify(totpDao, never()).confirmEnrollment(any(), any());

    }

    @Test
    public void testConfirmEnrollmentActivatesAndReturnsRecoveryCodes() throws Exception {

        final var secret = "JBSWY3DPEHPK3PXP";
        final var state = new TotpUserState();
        state.setUserId(USER_ID);
        state.setSecret(secret);
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(state));

        final var codeGenerator = new DefaultCodeGenerator();
        final var currentBucket = new SystemTimeProvider().getTime() / 30;
        final var validCode = codeGenerator.generate(secret, currentBucket);

        final var recoveryCodes = service.confirmEnrollment(validCode);

        assertEquals(recoveryCodes.getCodes().size(), 10);

        final var captor = ArgumentCaptor.forClass(List.class);
        verify(totpDao).confirmEnrollment(eq(USER_ID), captor.capture());
        assertEquals(captor.getValue().size(), 10);

        // Stored hashes must not equal the raw codes returned to the caller.
        assertFalse(captor.getValue().containsAll(recoveryCodes.getCodes()));

    }

    @Test
    public void testDisableTotpDelegatesToDao() {
        service.disableTotp();
        verify(totpDao).disable(USER_ID);
    }

    @Test
    public void testIsEnrolledReflectsState() {

        when(totpDao.findState(USER_ID)).thenReturn(Optional.empty());
        assertFalse(service.isEnrolled());

        final var enabledState = new TotpUserState();
        enabledState.setEnabled(true);
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(enabledState));
        assertTrue(service.isEnrolled());

    }

}
