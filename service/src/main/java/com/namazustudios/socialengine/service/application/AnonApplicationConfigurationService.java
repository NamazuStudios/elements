package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.ApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ProductBundle;
import com.namazustudios.socialengine.service.ApplicationConfigurationService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AnonApplicationConfigurationService implements ApplicationConfigurationService {

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public Pagination<ApplicationConfiguration> getApplicationProfiles(String applicationNameOrId, int offset, int count) {
        return getApplicationConfigurationDao().getActiveApplicationConfigurations(applicationNameOrId, offset, count);
    }

    @Override
    public Pagination<ApplicationConfiguration> getApplicationProfiles(String applicationNameOrId, int offset, int count, String search) {
        return getApplicationConfigurationDao().getActiveApplicationConfigurations(applicationNameOrId, offset, count, search);
    }

    @Override
    public ApplicationConfiguration updateProductBundle(final String applicationConfigurationId,
                                                        final ProductBundle productBundle) {
        throw new ForbiddenException("Unprivileged requests are unable to update product bundles.");
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
