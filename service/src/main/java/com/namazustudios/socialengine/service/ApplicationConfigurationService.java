package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ProductBundle;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

import java.util.List;

/**
 * Manages the lifecycle of the various {@link ApplicationConfiguration} instances.
 *
 * Created by patricktwohig on 7/13/15.
 */
@Expose({
    @ExposedModuleDefinition("namazu.elements.service.application.configuration"),
    @ExposedModuleDefinition(
        value = "namazu.elements.service.unscoped.application.configuration",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
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
