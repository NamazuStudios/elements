package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationConfigurationDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ProductBundle;
import com.namazustudios.socialengine.service.ApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class SuperUserApplicationConfigurationService implements ApplicationConfigurationService {

    @Inject
    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public Pagination<ApplicationConfiguration> getApplicationProfiles(String applicationNameOrId, int offset, int count) {
        return applicationConfigurationDao.getActiveApplicationConfigurations(applicationNameOrId, offset, count);
    }

    @Override
    public Pagination<ApplicationConfiguration> getApplicationProfiles(String applicationNameOrId,
                                                                       int offset, int count, String search) {
        return applicationConfigurationDao.getActiveApplicationConfigurations(applicationNameOrId, offset, count, search);
    }

    @Override
    public ApplicationConfiguration updateProductBundle(final String applicationConfigurationId,
                                                 final ProductBundle productBundle) {
        
    }

}
