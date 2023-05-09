package dev.getelements.elements.rt.lua.builtin;

import dev.getelements.elements.rt.annotation.CaseFormat;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import static dev.getelements.elements.rt.annotation.CaseFormat.LOWER_UNDERSCORE;
import static dev.getelements.elements.rt.annotation.CaseFormat.UPPER_UNDERSCORE;

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
    boolean isDeprecated();

    /**
     * Gets the deprecation warning.
     *
     * @return the deprecation warning.
     */
    String getDeprecationWarning();

    /**
     * Gets the {@link CaseFormat} used by this {@link BuiltinDefinition} when binding methods.
     *
     * @return the {@link CaseFormat}
     */
    CaseFormat getMethodCaseFormat();

    /**
     * Gets the {@link CaseFormat} used by this {@link BuiltinDefinition} when binding constants.
     *
     * @return the {@link CaseFormat}
     */
    CaseFormat getConstantCaseFormat();

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

            @Override
            public CaseFormat getMethodCaseFormat() {
                return LOWER_UNDERSCORE;
            }

            @Override
            public CaseFormat getConstantCaseFormat() {
                return UPPER_UNDERSCORE;
            }

        };
    }

    static BuiltinDefinition fromDefinition(final ModuleDefinition definition) {
        return new BuiltinDefinition() {

            @Override
            public String getModuleName() {
                return definition.value();
            }

            @Override
            public String getDeprecationWarning() {
                return definition.deprecated().value();
            }

            @Override
            public boolean isDeprecated() {
                return definition.deprecated().deprecated();
            }

            @Override
            public CaseFormat getMethodCaseFormat() {
                return definition.style().methodCaseFormat();
            }

            @Override
            public CaseFormat getConstantCaseFormat() {
                return definition.style().constantCaseFormat();
            }

        };
    }

}
