package dev.getelements.elements.testsources;

import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.ExposeEnum;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

/**
 * Test exposed enum.
 */
@ExposeEnum({
    @ModuleDefinition("test.javadoc.exposed.enum.foo"),
    @ModuleDefinition("test.javadoc.exposed.enum.bar"),
    @ModuleDefinition(
        value = "test.javadoc.exposed.enum.deprecated",
        deprecated = @DeprecationDefinition
    )
})
public enum ExposedEnumJavadoc {

    /**
     * Foo exposed.
     */
    FOO,

    /**
     * Bar exposed.
     */
    BAR

}
