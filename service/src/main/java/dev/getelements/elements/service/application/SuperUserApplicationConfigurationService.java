package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ProductBundle;

import dev.getelements.elements.sdk.service.application.ApplicationConfigurationService;
import jakarta.inject.Inject;
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
    public <T extends ApplicationConfiguration>
    T updateProductBundles(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final Class<T> configurationClass,
            final List<ProductBundle> productBundles) {
        return applicationConfigurationDao.updateProductBundles(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                configurationClass,
                productBundles
        );
    }

}
