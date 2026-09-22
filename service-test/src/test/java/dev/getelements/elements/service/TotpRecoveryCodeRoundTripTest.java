package dev.getelements.elements.service;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.model.auth.TotpUserState;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.service.auth.totp.DefaultTotpVerificationService;
import dev.getelements.elements.service.auth.totp.UserTotpEnrollmentService;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

/**
 * Full round trip through the REAL {@link UserTotpEnrollmentService} (real secret/recovery-code generation
 * and hashing) and the REAL {@link DefaultTotpVerificationService} (real verification and hashing), sharing
 * an in-memory fake {@link TotpDao} rather than a mock -- so a hash mismatch between the "store" and "verify"
 * sides would actually surface here, unlike a test that mocks consumeRecoveryCode to always return true.
 */
public class TotpRecoveryCodeRoundTripTest {

    private static final String USER_ID = "user-1";

    /** Minimal in-memory fake -- deliberately not a Mockito mock, so hashing logic actually runs both ways. */
    private static class InMemoryTotpDao implements TotpDao {

        private final Map<String, TotpUserState> states = new HashMap<>();

        @Override
        public Optional<TotpUserState> findState(String userId) {
            return Optional.ofNullable(states.get(userId));
        }

        @Override
        public TotpUserState beginEnrollment(String userId, String secret) {
            final var state = new TotpUserState();
            state.setUserId(userId);
            state.setSecret(secret);
            state.setEnabled(false);
            states.put(userId, state);
            return state;
        }

        @Override
        public TotpUserState confirmEnrollment(String userId, List<String> hashedRecoveryCodes) {
            final var state = states.get(userId);
            state.setEnabled(true);
            state.setRecoveryCodeHashes(hashedRecoveryCodes);
            return state;
        }

        @Override
        public void disable(String userId) {
            states.remove(userId);
        }

        @Override
        public boolean consumeRecoveryCode(String userId, String hashedCode) {
            final var state = states.get(userId);
            if (state == null || state.getRecoveryCodeHashes() == null) return false;
            final var removed = state.getRecoveryCodeHashes().remove(hashedCode);
            return removed;
        }

    }

    @Test
    public void testGeneratedRecoveryCodeVerifiesSuccessfullyAndIsSingleUse() {

        final var dao = new InMemoryTotpDao();

        final var enabledConfig = new TotpConfiguration();
        enabledConfig.setEnabled(true);
        final var totpConfigurationDao = mock(TotpConfigurationDao.class);
        when(totpConfigurationDao.getConfiguration()).thenReturn(enabledConfig);

        final var user = new User();
        user.setId(USER_ID);
        user.setName("someuser");
        user.setLevel(User.Level.USER);

        final var enrollmentService = new UserTotpEnrollmentService();
        enrollmentService.setUser(user);
        enrollmentService.setTotpConfigurationDao(totpConfigurationDao);
        enrollmentService.setTotpDao(dao);

        final var enrollment = enrollmentService.beginEnrollment();

        // Confirm with a real, currently-valid TOTP code for the generated secret.
        final var codeGenerator = new DefaultCodeGenerator();
        final long currentBucket;
        try {
            currentBucket = new SystemTimeProvider().getTime() / 30;
            final var validCode = codeGenerator.generate(enrollment.getSecret(), currentBucket);
            final var recoveryCodes = enrollmentService.confirmEnrollment(validCode);

            assertEquals(recoveryCodes.getCodes().size(), 10);

            final var verificationService = new DefaultTotpVerificationService();
            verificationService.setTotpConfigurationDao(totpConfigurationDao);
            verificationService.setTotpDao(dao);

            final var candidateCode = recoveryCodes.getCodes().get(0);

            // Exactly what the wire format does: JSON round-trips it as a plain string, api-client only
            // trims (no case change) before sending -- simulate that here rather than using the raw in-memory
            // Java String reference.
            final var asSubmittedByClient = candidateCode.trim();

            assertTrue(
                    verificationService.verify(user, asSubmittedByClient),
                    "A freshly generated recovery code must verify successfully"
            );

            // Single-use: the same code must fail the second time.
            assertFalse(
                    verificationService.verify(user, asSubmittedByClient),
                    "A recovery code must not be usable twice"
            );

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
