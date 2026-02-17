package dev.getelements.elements.sdk.local;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentBuilder;
import dev.getelements.elements.sdk.util.PropertiesAttributes;

import java.nio.file.Path;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A builder type for the {@link ElementsLocal} instance. Since 3.7 there are multiple changes to how the local SDK
 * works. Therefore, methods on this type have changed in function and intended use. Specifically, the local SDK now
 * loads Elements from the local artifact repository via the {@link ElementDeployment} type. Practically speaking,
 * Maven is the only artifact repository supported. However, the system is designed for that to be replaceable and
 * separate from system itself.
 */
public interface ElementsLocalBuilder {

    /**
     * Specifies the system properties as a {@link Properties} instance.
     *
     * @param properties properties
     * @return this instance
     */
    default ElementsLocalBuilder withProperties(final Properties properties) {
        final var attributes = PropertiesAttributes.wrap(properties);
        return withAttributes(attributes);
    }

    /**
     * Specifies the system properties as an {@link Attributes} instance.
     *
     * @param attributes the attributes
     * @return this instance
     */
    ElementsLocalBuilder withAttributes(Attributes attributes);

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param aPackage            the name of the Java package for the Element
     * @return this instance
     * @deprecated no longer supported
     */
    @Deprecated
    default ElementsLocalBuilder withElementNamed(
            final String applicationNameOrId,
            final String aPackage) {
        getLogger(getClass()).warn("withProperties is no longer supported. Slated for removal.");
        return withElementNamed(applicationNameOrId, aPackage, emptyAttributes());
    }

    /**
     * Specifies an {@link Element} to load associated with the supplied package.
     *
     * @param applicationNameOrId the name or id of the {@link Application}
     * @param elementName            the name of the Element
     * @param attributes the {@link Attributes} to use when loading the package
     * @return this instance
     * @deprecated no longer supported
     */
    @Deprecated
    default ElementsLocalBuilder withElementNamed(
            String applicationNameOrId,
            String elementName,
            Attributes attributes) {
        getLogger(getClass()).warn("withElementNamed is no longer supported.");
        return this;
    }

    /**
     * Uses the current working directory as the source root.
     */
    default ElementsLocalBuilder withSourceRoot() {
        return withSourceRoot(Path.of(".").toAbsolutePath());
    }

    /**
     * Specifies a project's source code root. This will command the build system to run the build and install it to
     * the
     * @return the project root
     */
    ElementsLocalBuilder withSourceRoot(Path path);

    /**
     * Configures an {@link ElementDeployment} based on the builder. The supplied {@link Consumer} must perform all
     * the operations to configure the {@link ElementDeployment} using the {@link ElementDeploymentBuilder} which will
     * be
     */
    ElementsLocalBuilder withDeployment(Consumer<ElementDeploymentBuilder> elementDeploymentBuilderConsumer);

    /**
     * Builds the {@link ElementsLocal} instance
     *
     * @return the {@link ElementsLocal} instance
     */
    ElementsLocal build();

    /**
     * Gets a default instance of the {@link ElementsLocalBuilder}.
     *
     * @return the {@link ElementsLocalBuilder}
     */
    static ElementsLocalBuilder getDefault() {
        return ServiceLoader
                .load(ElementsLocalBuilder.class)
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("No SPI for " + ElementsLocalBuilder.class.getName() + " found."))
                .get();
    }

}
