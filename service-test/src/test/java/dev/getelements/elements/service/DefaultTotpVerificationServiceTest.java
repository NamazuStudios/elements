package dev.getelements.elements.service;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.model.auth.TotpUserState;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.service.auth.totp.DefaultTotpVerificationService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class DefaultTotpVerificationServiceTest {

    private static final String USER_ID = "user-1";

    private TotpConfigurationDao totpConfigurationDao;

    private TotpDao totpDao;

    private DefaultTotpVerificationService service;

    @BeforeMethod
    public void setup() {

        totpConfigurationDao = mock(TotpConfigurationDao.class);
        totpDao = mock(TotpDao.class);

        service = new DefaultTotpVerificationService();
        service.setTotpConfigurationDao(totpConfigurationDao);
        service.setTotpDao(totpDao);

    }

    private static User userWithId(final String id) {
        final var user = new User();
        user.setId(id);
        return user;
    }

    private static TotpConfiguration config(final boolean enabled) {
        final var configuration = new TotpConfiguration();
        configuration.setEnabled(enabled);
        return configuration;
    }

    private static TotpUserState enabledState(final String secret, final List<String> recoveryCodeHashes) {
        final var state = new TotpUserState();
        state.setUserId(USER_ID);
        state.setSecret(secret);
        state.setEnabled(true);
        state.setRecoveryCodeHashes(recoveryCodeHashes);
        return state;
    }

    @Test
    public void testNotRequiredWhenSystemDisabled() {
        when(totpConfigurationDao.getConfiguration()).thenReturn(config(false));
        assertFalse(service.isRequiredFor(userWithId(USER_ID)));
        verifyNoInteractions(totpDao);
    }

    @Test
    public void testNotRequiredWhenUserNotEnrolled() {
        when(totpConfigurationDao.getConfiguration()).thenReturn(config(true));
        when(totpDao.findState(USER_ID)).thenReturn(Optional.empty());
        assertFalse(service.isRequiredFor(userWithId(USER_ID)));
    }

    @Test
    public void testRequiredWhenSystemEnabledAndUserEnrolled() {
        when(totpConfigurationDao.getConfiguration()).thenReturn(config(true));
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(enabledState("secret", List.of())));
        assertTrue(service.isRequiredFor(userWithId(USER_ID)));
    }

    @Test
    public void testVerifyAcceptsValidTotpCode() throws Exception {

        final var secret = "JBSWY3DPEHPK3PXP";
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(enabledState(secret, List.of())));

        final var codeGenerator = new DefaultCodeGenerator();
        final var currentBucket = new SystemTimeProvider().getTime() / 30;
        final var validCode = codeGenerator.generate(secret, currentBucket);

        assertTrue(service.verify(userWithId(USER_ID), validCode));

    }

    @Test
    public void testVerifyRejectsInvalidTotpCodeWithNoRecoveryCodeMatch() {
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(enabledState("JBSWY3DPEHPK3PXP", List.of())));
        when(totpDao.consumeRecoveryCode(eq(USER_ID), anyString())).thenReturn(false);
        assertFalse(service.verify(userWithId(USER_ID), "000000"));
    }

    @Test
    public void testVerifyFallsBackToRecoveryCode() {
        when(totpDao.findState(USER_ID)).thenReturn(Optional.of(enabledState("JBSWY3DPEHPK3PXP", List.of())));
        when(totpDao.consumeRecoveryCode(eq(USER_ID), anyString())).thenReturn(true);
        assertTrue(service.verify(userWithId(USER_ID), "abcd-1234-efgh-5678"));
    }

    @Test
    public void testVerifyFailsWhenNotEnrolled() {
        when(totpDao.findState(USER_ID)).thenReturn(Optional.empty());
        assertFalse(service.verify(userWithId(USER_ID), "123456"));
        verify(totpDao, never()).consumeRecoveryCode(any(), any());
    }

}
