package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.annotation.FacebookPermission;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;

import dev.getelements.elements.sdk.service.application.OculusApplicationConfigurationService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class AnonOculusApplicationConfigurationService implements OculusApplicationConfigurationService {

    private Supplier<List<FacebookPermission>> FacebookPermissionListSupplier;

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public OculusApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {

        final var oculusApplicationConfiguration = getApplicationConfigurationDao()
                .getApplicationConfiguration(
                        OculusApplicationConfiguration.class,
                        applicationNameOrId,
                        applicationConfigurationNameOrId
                );

        return redactSecretInformationAndApplyBuiltins(oculusApplicationConfiguration);

    }

    @Override
    public OculusApplicationConfiguration createApplicationConfiguration(String applicationNameOrId, OculusApplicationConfiguration oculusApplicationConfiguration) {
        throw new ForbiddenException();
    }

    @Override
    public OculusApplicationConfiguration updateApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId, OculusApplicationConfiguration oculusApplicationConfiguration) {
        throw new ForbiddenException();
    }

    private OculusApplicationConfiguration redactSecretInformationAndApplyBuiltins(final OculusApplicationConfiguration oculusApplicationConfiguration) {

        final List<String> builtinFacebookPermissions = getFacebookPermissionListSupplier()
                .get()
                .stream()
                .map(p -> p.value())
                .collect(Collectors.toList());

        oculusApplicationConfiguration.setApplicationSecret(null);
        oculusApplicationConfiguration.setBuiltinApplicationPermissions(builtinFacebookPermissions);

        return oculusApplicationConfiguration;
    }

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

    public Supplier<List<FacebookPermission>> getFacebookPermissionListSupplier() {
        return FacebookPermissionListSupplier;
    }

    @Inject
    public void setFacebookPermissionListSupplier(Supplier<List<FacebookPermission>> FacebookPermissionListSupplier) {
        this.FacebookPermissionListSupplier = FacebookPermissionListSupplier;
    }

}
