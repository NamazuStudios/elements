package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;

/**
 * Interface to wrangle the builtin definitions.
 */
public interface BuiltinDefinition {

    /**
     * Gets the module name from the definition.
     *
     * @return the module name
     */
    String getModuleName();

    /**
     * Returns true if the module has been deprecated.
     *
     * @return true if deprecated, false otherwise
     */
    default boolean isDeprecated() {
        return !getDeprecationWarning().isBlank();
    }

    /**
     * Gets the deprecation warning.
     *
     * @return the deprecation warning.
     */
    String getDeprecationWarning();

    static BuiltinDefinition fromModuleName(final String moduleName) {
        return new BuiltinDefinition() {
            @Override
            public String getModuleName() {
                return moduleName;
            }

            @Override
            public boolean isDeprecated() {
                return false;
            }

            @Override
            public String getDeprecationWarning() {
                return "";
            }
        };
    }

    static BuiltinDefinition fromDefinition(final ExposedModuleDefinition definition) {
        return new BuiltinDefinition() {
            @Override
            public String getModuleName() {
                return definition.value();
            }

            @Override
            public String getDeprecationWarning() {
                return definition.deprecated().value();
            }
        };
    }

}
