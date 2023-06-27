package dev.getelements.elements.rt.annotation;

/**
 * Specifies case format translation. This should accommodate most, if not all languages. This relies on the underlying
 * Guava library, but adds additional cases.
 */
public enum CaseFormat {

    /**
     * Indicates natural case, which means to use the underlying language's default format. When either case involves
     * natural case, no conversion will take effect.
     */
    NATURAL(null),

    /**
     * See {@link com.google.common.base.CaseFormat#LOWER_HYPHEN}
     */
    LOWER_HYPHEN(com.google.common.base.CaseFormat.LOWER_HYPHEN),

    /**
     * See {@link com.google.common.base.CaseFormat#LOWER_CAMEL}
     */
    LOWER_CAMEL(com.google.common.base.CaseFormat.LOWER_CAMEL),

    /**
     * See {@link com.google.common.base.CaseFormat#UPPER_CAMEL}
     */
    UPPER_CAMEL(com.google.common.base.CaseFormat.UPPER_CAMEL),

    /**
     * See {@link com.google.common.base.CaseFormat#LOWER_UNDERSCORE}
     */
    LOWER_UNDERSCORE(com.google.common.base.CaseFormat.LOWER_UNDERSCORE),

    /**
     * See {@link com.google.common.base.CaseFormat#UPPER_UNDERSCORE}
     */
    UPPER_UNDERSCORE(com.google.common.base.CaseFormat.UPPER_UNDERSCORE);

    private final com.google.common.base.CaseFormat delegate;

    CaseFormat(final com.google.common.base.CaseFormat delegate) {
        this.delegate = delegate;
    }

    /**
     * Converts this to the other case.
     *
     * @param other the other case
     * @param in the input stream
     * @return the input string converted to the requested case.
     */
    public final String to(final CaseFormat other, final String in) {
        return delegate == null || other.delegate == null ? in : delegate.to(other.delegate, in);
    }

}
