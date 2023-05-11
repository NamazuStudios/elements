package dev.getelements.elements.service;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.application.ProductBundle;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Manages the lifecycle of the various {@link ApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.application.configuration"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.application.configuration",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.application.configuration",
                deprecated = @DeprecationDefinition("Use eci.elements.service.application.configuration instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.application.configuration",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.application.configuration instead.")
        )
})
public interface ApplicationConfigurationService {

    /**
     * Gets the applications registered in the databse given the offset and count.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param offset the offset
     * @param count the count
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getApplicationProfiles(final String applicationNameOrId,
                                                                final int offset, final int count);

    /**
     * Gets the applications registered in the database given the offset and count.
     *
     * @param applicationNameOrId the {@link Application} name or id
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     *
     * @return a {@link Pagination} of {@link Application} instances
     */
    Pagination<ApplicationConfiguration> getApplicationProfiles(final String applicationNameOrId,
                                                                final int offset, final int count,
                                                                final String search);

    ApplicationConfiguration updateProductBundles(final String applicationConfigurationId,
                                                  final List<ProductBundle> productBundles);

}
