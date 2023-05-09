package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.PSNApplicationConfigurationDao;
import dev.getelements.elements.model.application.PSNApplicationConfiguration;
import dev.getelements.elements.service.PSNApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/24/17.
 */
public class SuperUserPSNApplicationConfigurationService implements PSNApplicationConfigurationService {

    private PSNApplicationConfigurationDao psnApplicationConfigurationDao;

    @Override
    public PSNApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationProfileNameOrId) {
        return getPsnApplicationConfigurationDao().getPSNApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    @Override
    public PSNApplicationConfiguration createApplicationConfiguration(String applicationNameOrId,
                                                                      PSNApplicationConfiguration psnApplicationProfile) {
        return getPsnApplicationConfigurationDao().createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, psnApplicationProfile);
    }

    @Override
    public PSNApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId,
                                                                      String applicationProfileNameOrId,
                                                                      PSNApplicationConfiguration psnApplicationProfile) {
        return getPsnApplicationConfigurationDao().updateApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId, psnApplicationProfile);
    }

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationProfileNameOrId) {
        getPsnApplicationConfigurationDao().softDeleteApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
    }

    public PSNApplicationConfigurationDao getPsnApplicationConfigurationDao() {
        return psnApplicationConfigurationDao;
    }

    @Inject
    public void setPsnApplicationConfigurationDao(PSNApplicationConfigurationDao psnApplicationConfigurationDao) {
        this.psnApplicationConfigurationDao = psnApplicationConfigurationDao;
    }

}
