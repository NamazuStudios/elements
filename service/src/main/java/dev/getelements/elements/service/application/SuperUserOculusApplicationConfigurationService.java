package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.annotation.FacebookPermission;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.service.application.OculusApplicationConfigurationService;
import jakarta.inject.Inject;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class SuperUserOculusApplicationConfigurationService implements OculusApplicationConfigurationService {

    private Supplier<List<FacebookPermission>> FacebookPermissionListSupplier;

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        getApplicationConfigurationDao().deleteApplicationConfiguration(
                OculusApplicationConfiguration.class,
                applicationNameOrId,
                applicationConfigurationNameOrId);
    }

    @Override
    public OculusApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final OculusApplicationConfiguration oculusApplicationConfiguration;

        oculusApplicationConfiguration = getApplicationConfigurationDao()
                .getApplicationConfiguration(
                        OculusApplicationConfiguration.class,
                        applicationNameOrId,
                        applicationConfigurationNameOrId
                );

        return applyBuiltins(oculusApplicationConfiguration);

    }

    @Override
    public OculusApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, OculusApplicationConfiguration oculusApplicationConfiguration) {
        final OculusApplicationConfiguration result;

        result = getApplicationConfigurationDao()
                .createApplicationConfiguration(applicationNameOrId, oculusApplicationConfiguration);

        return applyBuiltins(result);
    }

    @Override
    public OculusApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, OculusApplicationConfiguration oculusApplicationConfiguration) {

        final OculusApplicationConfiguration result;

        result = getApplicationConfigurationDao()
                .updateApplicationConfiguration(applicationNameOrId, oculusApplicationConfiguration);

        return applyBuiltins(result);

    }

    public Supplier<List<FacebookPermission>> getFacebookPermissionListSupplier() {
        return FacebookPermissionListSupplier;
    }

    private OculusApplicationConfiguration applyBuiltins(final OculusApplicationConfiguration oculusApplicationConfiguration) {

        final List<String> builtinFacebookPermissions = getFacebookPermissionListSupplier()
                .get()
                .stream()
                .map(p -> p.value())
                .collect(Collectors.toList());

        oculusApplicationConfiguration.setBuiltinApplicationPermissions(builtinFacebookPermissions);

        return oculusApplicationConfiguration;

    }

    @Inject
    public void setFacebookPermissionListSupplier(Supplier<List<FacebookPermission>> FacebookPermissionListSupplier) {
        this.FacebookPermissionListSupplier = FacebookPermissionListSupplier;
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

}
