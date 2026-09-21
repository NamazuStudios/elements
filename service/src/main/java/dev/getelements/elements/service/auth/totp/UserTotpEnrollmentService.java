package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.model.auth.TotpEnrollment;
import dev.getelements.elements.sdk.model.auth.TotpRecoveryCodes;
import dev.getelements.elements.sdk.model.auth.TotpUserState;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.TotpEnrollmentService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import jakarta.inject.Inject;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserTotpEnrollmentService implements TotpEnrollmentService {

    private static final String ISSUER = "Namazu Elements";

    private static final int RECOVERY_CODE_COUNT = 10;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();

    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    private final RecoveryCodeGenerator recoveryCodeGenerator = new RecoveryCodeGenerator();

    private User user;

    private TotpConfigurationDao totpConfigurationDao;

    private TotpDao totpDao;

    @Override
    public TotpEnrollment beginEnrollment() {

        if (!getTotpConfigurationDao().getConfiguration().isEnabled()) {
            throw new ForbiddenException("TOTP two-factor authentication is not enabled on this server.");
        }

        final var secret = getSecretGenerator().generate();
        getTotpDao().beginEnrollment(getUser().getId(), secret);

        final var qrData = new QrData.Builder()
                .label(getUser().getName())
                .secret(secret)
                .issuer(ISSUER)
                .build();

        final var enrollment = new TotpEnrollment();
        enrollment.setSecret(secret);
        enrollment.setOtpAuthUri(qrData.getUri());
        return enrollment;

    }

    @Override
    public TotpRecoveryCodes confirmEnrollment(final String code) {

        final var state = getTotpDao().findState(getUser().getId())
                .filter(s -> s.getSecret() != null)
                .orElseThrow(() -> new InvalidDataException("No pending TOTP enrollment. Call beginEnrollment first."));

        if (!getCodeVerifier().isValidCode(state.getSecret(), code)) {
            throw new ForbiddenException("Invalid authentication code.");
        }

        final var codes = getRecoveryCodeGenerator().generateCodes(RECOVERY_CODE_COUNT);

        final var hashedCodes = Arrays.stream(codes)
                .map(TotpRecoveryCodeHasher::hash)
                .collect(Collectors.toList());

        getTotpDao().confirmEnrollment(getUser().getId(), hashedCodes);

        final var recoveryCodes = new TotpRecoveryCodes();
        recoveryCodes.setCodes(List.of(codes));
        return recoveryCodes;

    }

    @Override
    public void disableTotp() {
        getTotpDao().disable(getUser().getId());
    }

    @Override
    public boolean isEnrolled() {
        return getTotpDao().findState(getUser().getId())
                .map(TotpUserState::isEnabled)
                .orElse(false);
    }

    public SecretGenerator getSecretGenerator() {
        return secretGenerator;
    }

    public CodeVerifier getCodeVerifier() {
        return codeVerifier;
    }

    public RecoveryCodeGenerator getRecoveryCodeGenerator() {
        return recoveryCodeGenerator;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public TotpConfigurationDao getTotpConfigurationDao() {
        return totpConfigurationDao;
    }

    @Inject
    public void setTotpConfigurationDao(TotpConfigurationDao totpConfigurationDao) {
        this.totpConfigurationDao = totpConfigurationDao;
    }

    public TotpDao getTotpDao() {
        return totpDao;
    }

    @Inject
    public void setTotpDao(TotpDao totpDao) {
        this.totpDao = totpDao;
    }

}
