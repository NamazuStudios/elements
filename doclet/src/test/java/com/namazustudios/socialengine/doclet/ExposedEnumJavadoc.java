package com.namazustudios.socialengine.doclet;

import com.namazustudios.socialengine.rt.annotation.DeprecationDefinition;
import com.namazustudios.socialengine.rt.annotation.ExposeEnum;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;

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
