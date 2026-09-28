package dev.getelements.elements.service.auth.totp;

import dev.getelements.elements.sdk.dao.TotpConfigurationDao;
import dev.getelements.elements.sdk.model.auth.CreateOrUpdateTotpConfigurationRequest;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.service.auth.TotpConfigurationService;
import jakarta.inject.Inject;

public class SuperUserTotpConfigurationService implements TotpConfigurationService {

    private TotpConfigurationDao totpConfigurationDao;

    @Override
    public TotpConfiguration getConfiguration() {
        return getTotpConfigurationDao().getConfiguration();
    }

    @Override
    public TotpConfiguration updateConfiguration(final CreateOrUpdateTotpConfigurationRequest request) {
        final var configuration = new TotpConfiguration();
        configuration.setEnabled(request.isEnabled());
        return getTotpConfigurationDao().updateConfiguration(configuration);
    }

    public TotpConfigurationDao getTotpConfigurationDao() {
        return totpConfigurationDao;
    }

    @Inject
    public void setTotpConfigurationDao(TotpConfigurationDao totpConfigurationDao) {
        this.totpConfigurationDao = totpConfigurationDao;
    }

}
