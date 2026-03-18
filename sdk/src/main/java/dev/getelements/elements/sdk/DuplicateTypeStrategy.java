package dev.getelements.elements.sdk;

import dev.getelements.elements.sdk.exception.SdkDuplicateClassError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Indicates how to handle a duplicate type situation. This occurs when a delegate class loader and the Element both
 * expose the same type. Depending on the situation, it may mak esnse
 */
public enum DuplicateTypeStrategy {

    /**
     * Logs a warning and uses the parent class.
     */
    PARENT {
        @Override
        public Class<?> handle(final Logger logger, final Class<?> fromParent, final Class<?> fromDelegate) {

            logger.debug("Duplicate class definition for `{}`: found in `{}` and `{}`. Using type from parent.",
                    fromParent.getName(),
                    fromParent.getClassLoader() == null
                            ? "bootstrap"
                            : fromParent.getClassLoader().getName(),
                    fromDelegate.getClassLoader() == null
                            ? "bootstrap"
                            : fromDelegate.getClassLoader().getName()
            );

            return fromParent;

        }
    },

    /**
     * Logs a warning and uses the delegate class.
     */
    DELEGATE {
        @Override
        public Class<?> handle(final Logger logger, final Class<?> fromParent, final Class<?> fromDelegate) {

            logger.debug("Duplicate class definition for `{}`: found in `{}` and `{}`. Using type from delegate.",
                    fromParent.getName(),
                    fromParent.getClassLoader() == null
                            ? "bootstrap"
                            : fromParent.getClassLoader().getName(),
                    fromDelegate.getClassLoader() == null
                            ? "bootstrap"
                            : fromDelegate.getClassLoader().getName()
            );

            return fromDelegate;

        }
    },

    /**
     * Throws a linkage error.
     */
    LINKAGE_ERROR {
        @Override
        public Class<?> handle(final Logger logger, final Class<?> fromParent, final Class<?> fromDelegate) {
            throw new SdkDuplicateClassError(
                    "Duplicate class definition for `%s`: found in `%s` and `%s`.".formatted(
                            fromParent.getName(),
                            fromParent.getClassLoader() == null
                                    ? "bootstrap"
                                    : fromParent.getClassLoader().getName(),
                            fromDelegate.getClassLoader() == null
                                    ? "bootstrap"
                                    : fromDelegate.getClassLoader().getName()
                    )
            );
        }
    };

    public static DuplicateTypeStrategy DEFAULT;

    /**
     * Indicates the duplicate type strategy for permitted types.
     */
    public static final String DUPLICATE_TYPE_STRATEGY_KEY = "dev.getelements.elements.sdk.duplicate.type.strategy";

    static {

        final var duplicateTypeStrategy = System.getProperty(
                DUPLICATE_TYPE_STRATEGY_KEY,
                DuplicateTypeStrategy.LINKAGE_ERROR.name()
        );

        DEFAULT = Stream.of(DuplicateTypeStrategy.values())
                .filter(s -> duplicateTypeStrategy.equals(s.name()))
                .findFirst()
                .orElse(DuplicateTypeStrategy.LINKAGE_ERROR);

    }


    protected final Logger logger = LoggerFactory.getLogger(DuplicateTypeStrategy.class);

    /**
     * Handles the discrepancy according to the strategy.
     *
     * @param logger       a logger used to log messages
     * @param fromParent   the class from the parent
     * @param fromDelegate the class from the delegate
     * @return the class selected
     */
    public abstract Class<?> handle(Logger logger, Class<?> fromParent, Class<?> fromDelegate);
}
