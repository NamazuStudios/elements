package dev.getelements.elements.rt.annotation;

import java.lang.annotation.*;

import static dev.getelements.elements.rt.annotation.CaseFormat.*;

/**
 * Indicates the exposed code style.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CodeStyle {

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat methodCaseFormat() default LOWER_UNDERSCORE;

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method parameter bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat parameterCaseFormat() default LOWER_UNDERSCORE;

    /**
     * When exposing this module to the other language this will specify the case conversion to apply when generating
     * the method parameter bindings. By default this is {@link CaseFormat#LOWER_UNDERSCORE}
     *
     * @return the {@link CaseFormat} to use in conversion
     */
    CaseFormat constantCaseFormat() default UPPER_UNDERSCORE;

    /**
     * Used for converting the case format for types.
     *
     * @return the type case format
     */
    CaseFormat typeCaseFormat() default NATURAL;

    /**
     * Used for converting the case format for properties.
     *
     * @return the property case format
     */
    CaseFormat propertyCaseFormat() default NATURAL;

    /**
     * Indicates the method name prefix.
     */
    String methodPrefix() default "";

    /**
     * Implementation of {@link CodeStyle} which represents the JVM native code style.
     */
    CodeStyle JVM_NATIVE = new CodeStyle() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return CodeStyle.class;
        }

        @Override
        public CaseFormat methodCaseFormat() {
            return LOWER_CAMEL;
        }

        @Override
        public CaseFormat parameterCaseFormat() {
            return LOWER_CAMEL;
        }

        @Override
        public CaseFormat constantCaseFormat() {
            return UPPER_UNDERSCORE;
        }

        @Override
        public CaseFormat typeCaseFormat() {
            return UPPER_CAMEL;
        }

        @Override
        public CaseFormat propertyCaseFormat() {
            return LOWER_CAMEL;
        }

        @Override
        public String methodPrefix() {
            return "";
        }

        @Override
        protected Object clone() {
            return this;
        }

        @Override
        public int hashCode() {
            return
                (127 * "methodCaseFormat".hashCode()) ^ methodCaseFormat().hashCode() +
                (127 * "parameterCaseFormat".hashCode()) ^ parameterCaseFormat().hashCode() +
                (127 * "constantCaseFormat".hashCode()) ^ constantCaseFormat().hashCode() +
                (127 * "typeCaseFormat".hashCode()) ^ typeCaseFormat().hashCode() +
                (127 * "propertyCaseFormat".hashCode()) ^ propertyCaseFormat().hashCode() +
                (127 * "methodPrefix".hashCode()) ^ propertyCaseFormat().hashCode();
        }

        @Override
        public boolean equals(final Object obj) {

            if (!(obj instanceof CodeStyle)) {
                return false;
            }

            final CodeStyle other = (CodeStyle) obj;

            return
                methodCaseFormat().equals(other.methodCaseFormat()) &&
                parameterCaseFormat().equals(other.parameterCaseFormat()) &&
                constantCaseFormat().equals(other.constantCaseFormat()) &&
                typeCaseFormat().equals(other.typeCaseFormat()) &&
                propertyCaseFormat().equals(other.propertyCaseFormat()) &&
                methodPrefix().equals(other.methodPrefix());

        }

        @Override
        public String toString() {
            return "@" + CodeStyle.class.getName() + "(" +
                "methodCaseFormat=" + methodCaseFormat() + ", " +
                "parameterCaseFormat=" + parameterCaseFormat() + ", " +
                "constantCaseFormat=" + constantCaseFormat() + ", " +
                "typeCaseFormat=" + typeCaseFormat() + ", " +
                "propertyCaseFormat=" + propertyCaseFormat() + ", " +
                "methodPrefix=" + methodPrefix() +
            ")";
        }

    };

}
