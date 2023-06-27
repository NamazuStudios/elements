package dev.getelements.elements.doclet.metadata;

import dev.getelements.elements.rt.annotation.CaseFormat;
import dev.getelements.elements.rt.annotation.CodeStyle;

import static dev.getelements.elements.rt.annotation.CaseFormat.LOWER_CAMEL;
import static dev.getelements.elements.rt.annotation.CaseFormat.UPPER_UNDERSCORE;

/**
 * Defines code style.
 */
public interface CodeStyleMetadata {

    /**
     * The Java 11 code style.
     */
    CodeStyleMetadata JAVA_11 = new CodeStyleMetadata() {
        @Override
        public CaseFormat getMethodCaseFormat() {
            return LOWER_CAMEL;
        }

        @Override
        public CaseFormat getParameterCaseFormat() {
            return LOWER_CAMEL;
        }

        @Override
        public CaseFormat getConstantCaseFormat() {
            return UPPER_UNDERSCORE;
        }
    };

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat getMethodCaseFormat();

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method parameter bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat getParameterCaseFormat();

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method parameter bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat getConstantCaseFormat();

    /**
     * Generates a {@link CodeStyleMetadata} from the supplied {@link CodeStyle}.
     *
     * @param style the {@link CodeStyle} instance
     * @return the {@link CodeStyleMetadata} instance
     */
    static CodeStyleMetadata from(final CodeStyle style) {
        return new CodeStyleMetadata() {

            @Override
            public CaseFormat getMethodCaseFormat() {
                return style.methodCaseFormat();
            }

            @Override
            public CaseFormat getParameterCaseFormat() {
                return style.parameterCaseFormat();
            }

            @Override
            public CaseFormat getConstantCaseFormat() {
                return style.constantCaseFormat();
            }

        };
    }

}
