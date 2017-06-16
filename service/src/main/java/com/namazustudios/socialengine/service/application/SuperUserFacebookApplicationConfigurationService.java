package com.namazustudios.socialengine.service.application;

import com.namazustudios.socialengine.annotation.FacebookPermission;
import com.namazustudios.socialengine.dao.FacebookApplicationConfigurationDao;
import com.namazustudios.socialengine.model.application.FacebookApplicationConfiguration;
import com.namazustudios.socialengine.service.FacebookApplicationConfigurationService;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class SuperUserFacebookApplicationConfigurationService implements FacebookApplicationConfigurationService {

    private Supplier<List<FacebookPermission>> facebookPermissionListSupplier;

    private FacebookApplicationConfigurationDao facebookApplicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        getFacebookApplicationConfigurationDao()
            .softDeleteApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);
    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        final FacebookApplicationConfiguration facebookApplicationConfiguration;

        facebookApplicationConfiguration = getFacebookApplicationConfigurationDao()
            .getApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId);

        return applyBuiltins(facebookApplicationConfiguration);

    }

    @Override
    public FacebookApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {
        final FacebookApplicationConfiguration result;

        result = getFacebookApplicationConfigurationDao()
            .createOrUpdateInactiveApplicationConfiguration(applicationNameOrId, facebookApplicationConfiguration);

        return applyBuiltins(result);
    }

    @Override
    public FacebookApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final FacebookApplicationConfiguration result;

        result = getFacebookApplicationConfigurationDao()
            .updateApplicationConfiguration(applicationNameOrId, applicationConfigurationNameOrId, facebookApplicationConfiguration);

        return applyBuiltins(result);

    }

    public Supplier<List<FacebookPermission>> getFacebookPermissionListSupplier() {
        return facebookPermissionListSupplier;
    }

    private FacebookApplicationConfiguration applyBuiltins(final FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final List<String> builtinFacebookPermissions = getFacebookPermissionListSupplier()
            .get()
            .stream()
            .map(p -> p.value())
            .collect(Collectors.toList());

        facebookApplicationConfiguration.setBuiltinApplicationPermissions(builtinFacebookPermissions);

        return facebookApplicationConfiguration;

    }

    @Inject
    public void setFacebookPermissionListSupplier(Supplier<List<FacebookPermission>> facebookPermissionListSupplier) {
        this.facebookPermissionListSupplier = facebookPermissionListSupplier;
    }

    public FacebookApplicationConfigurationDao getFacebookApplicationConfigurationDao() {
        return facebookApplicationConfigurationDao;
    }

    @Inject
    public void setFacebookApplicationConfigurationDao(FacebookApplicationConfigurationDao facebookApplicationConfigurationDao) {
        this.facebookApplicationConfigurationDao = facebookApplicationConfigurationDao;
    }

}
