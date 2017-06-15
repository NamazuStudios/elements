import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class SuperUserFacebookApplicationConfigurationService implements FacebookApplicationConfigurationService {

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        getFacebookApplicationConfigurationDao().softDeleteApplicationProfile(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        return getFacebookApplicationConfigurationDao().getIosApplicationProfile(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public FacebookApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {
        return getFacebookApplicationConfigurationDao().createOrUpdateInactiveApplicationProfile(applicationNameOrId, facebookApplicationConfiguration);
    }

    @Override
    public FacebookApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {
        return getFacebookApplicationConfigurationDao().updateApplicationProfile(applicationNameOrId, applicationConfigurationNameOrId, facebookApplicationConfiguration);
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

}
