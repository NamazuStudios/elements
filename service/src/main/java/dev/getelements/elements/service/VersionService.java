package dev.getelements.elements.service;

import dev.getelements.elements.model.Version;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Gets the {@link Version} of the current build.
 *
 */
@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.version"
        ),
        @ModuleDefinition(
                value = "eci.elements.service.unscoped.version",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.version",
                deprecated = @DeprecationDefinition("Use eci.elements.service.version instead.")
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.version",
                annotation = @ExposedBindingAnnotation(Unscoped.class),
                deprecated = @DeprecationDefinition("Use eci.elements.service.unscoped.version instead.")
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
