package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.dao.IosApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.application.AppleSignInConfiguration;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.service.IosApplicationConfigurationService;
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
        return redactPrivateInformation(applicationConfiguration);
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


    private IosApplicationConfiguration redactPrivateInformation(final IosApplicationConfiguration iosApplicationConfiguration) {

        final IosApplicationConfiguration redacted = getMapper().map(iosApplicationConfiguration, IosApplicationConfiguration.class);
        final AppleSignInConfiguration redactedSignInConfiguration = redacted.getAppleSignInConfiguration();

        if (redactedSignInConfiguration != null) {
            redactedSignInConfiguration.setKeyId(null);
            redactedSignInConfiguration.setTeamId(null);
            redactedSignInConfiguration.setAppleSignInPrivateKey(null);
        }

        redacted.setAppleSignInConfiguration(redactedSignInConfiguration);

        return redacted;
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
