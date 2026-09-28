package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.model.auth.TotpUserState;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.auth.TotpVerificationService;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import jakarta.inject.Inject;

public class DefaultTotpVerificationService implements TotpVerificationService {

    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    private TotpConfigurationDao totpConfigurationDao;

    private TotpDao totpDao;

    @Override
    public boolean isRequiredFor(final User user) {

        if (!getTotpConfigurationDao().getConfiguration().isEnabled()) {
            return false;
        }

        return getTotpDao().findState(user.getId())
                .map(TotpUserState::isEnabled)
                .orElse(false);

    }

    @Override
    public boolean verify(final User user, final String code) {

        final var state = getTotpDao().findState(user.getId()).filter(TotpUserState::isEnabled);

        if (state.isEmpty()) {
            return false;
        }

        if (getCodeVerifier().isValidCode(state.get().getSecret(), code)) {
            return true;
        }

        // Not a valid current TOTP code -- try it as a one-time recovery code instead.
        final var hashed = TotpRecoveryCodeHasher.hash(code);
        return getTotpDao().consumeRecoveryCode(user.getId(), hashed);

    }

    public CodeVerifier getCodeVerifier() {
        return codeVerifier;
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
