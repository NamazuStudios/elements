package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.model.Version;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

/**
 * Gets the {@link Version} of the current build.
 *
 */
@Expose({
    @ModuleDefinition(value = "namazu.elements.service.version"),
    @ModuleDefinition(
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
