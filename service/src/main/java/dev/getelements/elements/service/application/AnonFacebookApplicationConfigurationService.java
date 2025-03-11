package dev.getelements.elements.service.application;

import dev.getelements.elements.sdk.model.annotation.FacebookPermission;
import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;

import dev.getelements.elements.sdk.service.application.FacebookApplicationConfigurationService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 6/26/17.
 */
public class AnonFacebookApplicationConfigurationService implements FacebookApplicationConfigurationService {

    private Supplier<List<FacebookPermission>> facebookPermissionListSupplier;

    private ApplicationConfigurationDao applicationConfigurationDao;

    @Override
    public void deleteApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {
        throw new ForbiddenException();
    }

    @Override
    public FacebookApplicationConfiguration getApplicationConfiguration(String applicationNameOrId, String applicationConfigurationNameOrId) {

        final var facebookApplicationConfiguration = getApplicationConfigurationDao()
            .getApplicationConfiguration(
                    FacebookApplicationConfiguration.class,
                    applicationNameOrId,
                    applicationConfigurationNameOrId
            );

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

    public ApplicationConfigurationDao getApplicationConfigurationDao() {
        return applicationConfigurationDao;
    }

    @Inject
    public void setApplicationConfigurationDao(ApplicationConfigurationDao applicationConfigurationDao) {
        this.applicationConfigurationDao = applicationConfigurationDao;
    }

    public Supplier<List<FacebookPermission>> getFacebookPermissionListSupplier() {
        return facebookPermissionListSupplier;
    }

    @Inject
    public void setFacebookPermissionListSupplier(Supplier<List<FacebookPermission>> facebookPermissionListSupplier) {
        this.facebookPermissionListSupplier = facebookPermissionListSupplier;
    }

}
