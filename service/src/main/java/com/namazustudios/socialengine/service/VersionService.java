package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Gets the {@link Version} of the current build.
 *
 */
@Expose({
    @ExposedModuleDefinition(value = "namazu.elements.service.version"),
    @ExposedModuleDefinition(
        value = "namazu.elements.service.unscoped.version",
        annotation = @ExposedBindingAnnotation(Unscoped.class)
    )
})
public interface VersionService {

    /**
     * Returns the current {@link Version} of the current built.
     *
     * @return the {@link Version}
     */
    Version getVersion();

}
