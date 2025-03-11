package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.application.IosApplicationConfigurationService;
import jakarta.inject.Inject;

public class AnonIosApplicationConfigurationService implements IosApplicationConfigurationService {

    private MapperRegistry mapperRegistry;

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationProfileNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public IosApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                   final String applicationProfileNameOrId) {
        return getApplicationConfigurationDao()
            .getApplicationConfiguration(
                    IosApplicationConfiguration.class,
                    applicationNameOrId,
                    applicationProfileNameOrId
            );
    }

    @Override
    public IosApplicationConfiguration createApplicationConfiguration(
            final String applicationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public IosApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final IosApplicationConfiguration iosApplicationConfiguration) {
        throw new ForbiddenException();
    }


    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
