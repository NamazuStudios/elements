package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.dao.TotpDao;
import dev.getelements.elements.sdk.service.auth.TotpAdminService;
import jakarta.inject.Inject;

public class SuperUserTotpAdminService implements TotpAdminService {

    private TotpDao totpDao;

    @Override
    public void resetTotp(final String userId) {
        getTotpDao().disable(userId);
    }

    public TotpDao getTotpDao() {
        return totpDao;
    }

    @Inject
    public void setTotpDao(TotpDao totpDao) {
        this.totpDao = totpDao;
    }

}
