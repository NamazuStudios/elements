package dev.getelements.elements.sdk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;

/**
 * Enables the SLF4J logging framework to be shared among system, SDK, and Element. Additionally, this allow for
 * additional logging packages to be enabled for use with SLF4J. Placing a file "elements-slf4j.properties" on the
 * classpath will enable Elements to use SLF4J for logging. This file should contain the packages of the loggers to be
 * visible to all elements.
 * 
 * The properties fill is a standard Java properties file, and should contain the package names as keys, with
 * the value of "true" or "false" indicating if they should be permitted. If the value is blank, the package is
 * presumed to be permitted. Otherwise, the value is interpreted using {@link Boolean#parseBoolean(String)}.
 */
public class LoggingPermittedPackages implements PermittedPackages {

    private static final Logger logger = LoggerFactory.getLogger(LoggingPermittedPackages.class);

    private static final String WILDCARD_SUFFIX = ".*";

    public static final String ELEMENTS_SFL4J_PROPERTIES = "elements-slf4j-packages.properties";

    private static final List<Predicate<Package>> CONFIGURED_PACKAGES = loadFromProperties().orElseGet(List::of);

    private static Optional<List<Predicate<Package>>> loadFromProperties() {
        try (final var is = LoggingPermittedPackages.class.getResourceAsStream(ELEMENTS_SFL4J_PROPERTIES)) {

            if (is == null)
                return Optional.empty();

            final var properties = new Properties();
            properties.load(is);

            final List<Predicate<Package>> predicates = properties.entrySet()
                    .stream()
                    .filter(e -> e.getValue() == null || Boolean.parseBoolean(e.getValue().toString()))
                    .map(e -> e.getKey().toString())
                    .map(aPackageName -> aPackageName.endsWith(WILDCARD_SUFFIX)

                        // Wildcard Case
                        ? (Predicate<Package>)p -> p
                            .getName()
                            .startsWith(aPackageName.substring(0, aPackageName.length() - WILDCARD_SUFFIX.length()))

                        // Non-Wildcard Case
                        : (Predicate<Package>)p -> p
                            .getName()
                            .equals(aPackageName)

                    ).toList();

            return Optional.of(predicates);

        } catch (FileNotFoundException ex) {
            logger.trace("Failed to load SLF4J permitted packages from properties file: {}", ELEMENTS_SFL4J_PROPERTIES, ex);
            return Optional.empty();
        } catch (IOException ex) {
            logger.warn("Failed to load SLF4J permitted packages from properties file: {}", ELEMENTS_SFL4J_PROPERTIES, ex);
            return Optional.empty();
        }
    }

    private static final List<String> PERMITTED_PACKAGES = List.of(
            "org.slf4j",
            "org.slf4j.spi",
            "org.slf4j.event",
            "org.slf4j.helpers"
    );

    @Override
    public boolean test(final Package aPackage) {
        return PERMITTED_PACKAGES.contains(aPackage.getName()) || CONFIGURED_PACKAGES
                .stream()
                .anyMatch(p -> p.test(aPackage));
    }

    @Override
    public String getDescription() {
        return "Permits sl4fj APIs as well as allows an Element to request additional logging from the system.";
    }

}
