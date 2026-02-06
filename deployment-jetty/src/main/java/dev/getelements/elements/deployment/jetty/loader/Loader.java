package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.deployment.ElementRuntimeService.RuntimeRecord;
import dev.getelements.elements.sdk.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.IllegalFormatException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;


public interface Loader {

    /**
     * Loads the {@link Element}s from the supplied {@link RuntimeRecord}
     *
     * @param pending the pending deployment context
     * @param record  the record to load
     */
    default void load(final PendingDeployment pending, final RuntimeRecord record) {
        record.elements().forEach(element -> load(pending, record, element));
    }

    /**
     * Loads the specific {@link Element} from the supplied {@link RuntimeRecord}
     *
     * @param record the record to load
     */
    void load(PendingDeployment pending, RuntimeRecord record, Element element);

    /**
     * Provides context and  callbacks for pending deployments.
     *
     * @param uris a predicate that returns true if the URI is new, false if it has already been recorded.
     * @param logs a consumer of log messages
     * @param warnings a consumer of warning messages
     * @param errors a consumer of errors
     * @param elements a consumer of successfully loaded elements
     */
    record PendingDeployment(
            Predicate<URI> uris,
            Consumer<String> logs,
            Consumer<String> warnings,
            Consumer<Throwable> errors,
            Consumer<Element> elements) {

        private static final Logger logger = LoggerFactory.getLogger(PendingDeployment.class);

        public void log(final String message) {
            log(logs(), message);
        }

        public void logf(final String fmt, final Object ... args) {
            logf(logs(), fmt, args);
        }

        public void logWarning(final String message) {
            log(warnings(), message);
        }

        public void logWarningf(final String fmt, final Object ... args) {
            logf(warnings(), fmt, args);
        }

        /**
         * Logs a message to the deployment log.
         *
         * @param message the message
         */
        public static void log(final Consumer<String> logs, final String message) {
            logs.accept(message);
        }

        /**
         * Logs a formatted message to the deployment log.
         *
         * @param format the format
         * @param args the arguments
         */
        public static void logf(final Consumer<String> log, final String format, final Object... args) {
            try {
                final var formatted = format(format, args);
                log(log, formatted);
            } catch (IllegalFormatException ex) {

                logger.error("Badly formatted log message.", ex);

                final var aggregate = Stream.of(args)
                        .map(o -> o == null ? "null" : o.toString())
                        .collect(joining(","));

                final var formatted = format("Bad log message format: %s (args: %s) - %s",
                        format,
                        aggregate,
                        ex.getMessage()
                );

                log(log, formatted);

            }

        }

        /**
         * Records a URI to the deployment record.
         *
         * @param uri the uri
         */
        public void uri(final URI uri) {
            if (!uris().test(uri)) {
                logf("Warning! Detected duplicate URI: %s", uri);
            }
        }

        /**
         * Records a Throwable to the deployment errors. Returns the same throwable for convenience such that it may
         * be re-thrown.
         *
         * @param throwable the throwable
         * @return the same throwable
         */
        public <ThrowableT extends Throwable> ThrowableT warning(final ThrowableT throwable) {

            logf("WARNING! Caught Exception %s: - %s",
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage()
            );

            return throwable;

        }

        /**
         * Records a Throwable to the deployment errors. Returns the same throwable for convenience such that it may
         * be re-thrown.
         *
         * @param throwable the throwable
         * @return the same throwable
         */
        public <ThrowableT extends Throwable> ThrowableT error(final ThrowableT throwable) {

            errors.accept(throwable);

            logf("ERROR! Caught Exception %s: - %s",
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage()
            );

            return throwable;

        }

        /**
         * Records that an element was successfully loaded to the container.
         *
         * @param element the element that was loaded
         */
        public void element(final Element element) {
            elements.accept(element);
        }

    }

}
