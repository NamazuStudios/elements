package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AnonFacebookApplicationConfigurationService implements FacebookApplicationConfigurationService {

    private Supplier<List<FacebookPermission>> facebookPermissionListSupplier;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {

        final FacebookApplicationConfiguration facebookApplicationConfiguration =
            getFacebookApplicationConfigurationDao()
            .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);

        return redactSecretInformationAndApplyBuiltins(facebookApplicationConfiguration);

    }

    @Override
    public FacebookApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public FacebookApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {
        throw new ForbiddenException();
    }

    private FacebookApplicationConfiguration redactSecretInformationAndApplyBuiltins(final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final List<String> builtinFacebookPermissions = getFacebookPermissionListSupplier()
                .get()
                .stream()
                .map(p -> p.value())
                .collect(Collectors.toList());

        facebookApplicationConfiguration.setApplicationSecret(null);
        facebookApplicationConfiguration.setBuiltinApplicationPermissions(builtinFacebookPermissions);

        return facebookApplicationConfiguration;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

    public Supplier<List<FacebookPermission>> getFacebookPermissionListSupplier() {
        return facebookPermissionListSupplier;
    }

    @Inject
    public void setFacebookPermissionListSupplier(Supplier<List<FacebookPermission>> facebookPermissionListSupplier) {
        this.facebookPermissionListSupplier = facebookPermissionListSupplier;
    }

}
