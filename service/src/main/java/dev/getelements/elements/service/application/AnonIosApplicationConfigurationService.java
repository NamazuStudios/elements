package dev.getelements.elements.service.application;

import dev.getelements.elements.dao.IosApplicationConfigurationDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.application.IosApplicationConfiguration;
import dev.getelements.elements.service.IosApplicationConfigurationService;
import org.dozer.Mapper;

import javax.inject.Inject;

public class AnonIosApplicationConfigurationService implements IosApplicationConfigurationService {

    private Mapper mapper;

    private IosApplicationConfigurationDao iosApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(final String applicationNameOrId,
                                               final String applicationProfileNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public IosApplicationConfiguration getApplicationConfiguration(final String applicationNameOrId,
                                                                   final String applicationProfileNameOrId) {
        final IosApplicationConfiguration applicationConfiguration = getIosApplicationConfigurationDao()
            .getIosApplicationConfiguration(applicationNameOrId, applicationProfileNameOrId);
        return applicationConfiguration;
    }

    @Override
    public IosApplicationConfiguration createApplicationConfiguration(final String applicationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public IosApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId,
                                                                      final IosApplicationConfiguration iosApplicationConfiguration) {
        throw new ForbiddenException();
    }


    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public IosApplicationConfigurationDao getIosApplicationConfigurationDao() {
        return iosApplicationConfigurationDao;
    }

    @Inject
    public void setIosApplicationConfigurationDao(IosApplicationConfigurationDao iosApplicationConfigurationDao) {
        this.iosApplicationConfigurationDao = iosApplicationConfigurationDao;
    }

}
