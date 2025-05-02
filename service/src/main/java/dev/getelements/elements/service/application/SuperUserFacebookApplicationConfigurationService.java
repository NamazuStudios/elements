package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.annotation.FacebookPermission;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.FacebookApplicationConfigurationService;
import jakarta.inject.Inject;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class SuperUserFacebookApplicationConfigurationService implements FacebookApplicationConfigurationService {

    private Supplier<List<FacebookPermission>> facebookPermissionListSupplier;

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                FacebookApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final FacebookApplicationConfiguration facebookApplicationConfiguration;

        facebookApplicationConfiguration = getApplicationConfigurationDao()
            .getApplicationConfiguration(
                    FacebookApplicationConfiguration.class,
                    applicationNameOrId,
                    applicationConfigurationNameOrId
            );

        return applyBuiltins(facebookApplicationConfiguration);

    }

    @Override
    public FacebookApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {
        final FacebookApplicationConfiguration result;

        result = getApplicationConfigurationDao()
            .createApplicationConfiguration(applicationNameOrId, facebookApplicationConfiguration);

        return applyBuiltins(result);
    }

    @Override
    public FacebookApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, FacebookApplicationConfiguration facebookApplicationConfiguration) {

        final FacebookApplicationConfiguration result;

        result = getApplicationConfigurationDao()
            .updateApplicationConfiguration(applicationNameOrId, facebookApplicationConfiguration);

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

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
