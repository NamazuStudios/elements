package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.ApplicationConfigurationDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.ProductBundle;
import dev.getelements.elements.service.ApplicationConfigurationService;

import javax.inject.Inject;
import java.util.List;

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
    public ApplicationConfiguration updateProductBundles(final String applicationConfigurationId,
                                                 final List<ProductBundle> productBundles) {
        return applicationConfigurationDao.updateProductBundles(applicationConfigurationId, productBundles);
    }

}
