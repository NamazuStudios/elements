package dev.getelements.elements.service;

import dev.getelements.elements.model.Version;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

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
