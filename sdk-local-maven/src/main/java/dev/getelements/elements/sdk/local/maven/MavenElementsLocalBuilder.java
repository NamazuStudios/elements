package dev.getelements.elements.sdk.local.maven;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.local.*;
import dev.getelements.elements.sdk.local.internal.ClasspathUtils;

import java.util.*;

import static dev.getelements.elements.sdk.local.maven.Maven.mvn;
import static java.util.Objects.requireNonNull;

public class MavenElementsLocalBuilder implements ElementsLocalBuilder {

    public static final String MAVEN_PHASE;

    public static final String ELEMENT_CLASSPATH;

    public static final String MAVEN_PHASE_ENV = "MAVEN_PHASE";

    public static final String ELEMENT_CLASSPATH_ENV = "SDK_LOCAL_CLASSPATH";

    public static final String MAVEN_PHASE_PROPERTY = "dev.getelements.elements.mvn.phase";

    public static final String ELEMENT_CLASSPATH_PROPERTY = "dev.getelements.elements.mvn.element.classpath";

    static {

        final var pathSeparator = System.getProperty("path.separator");

        final var defaultElementClasspath = String.join(
                pathSeparator,
                "target/classes",
                "target/element-libs/*"
        );

        MAVEN_PHASE = System.getenv(MavenElementsLocalBuilder.MAVEN_PHASE_ENV) != null
                ? System.getenv(MavenElementsLocalBuilder.MAVEN_PHASE_ENV)
                : System.getProperty(MavenElementsLocalBuilder.MAVEN_PHASE_PROPERTY, "generate-resources");

        ELEMENT_CLASSPATH = System.getenv(MavenElementsLocalBuilder.ELEMENT_CLASSPATH_ENV) != null
                ? System.getenv(MavenElementsLocalBuilder.ELEMENT_CLASSPATH_ENV)
                : System.getProperty(MavenElementsLocalBuilder.ELEMENT_CLASSPATH_PROPERTY, defaultElementClasspath);

        if (!MAVEN_PHASE.isBlank()) {
            mvn(MAVEN_PHASE);
        }

    }

    private Attributes attributes = Attributes.emptyAttributes();

    private final List<ElementsLocalApplicationElementRecord> localElements = new ArrayList<>();

    @Override
    public ElementsLocalBuilder withAttributes(final Attributes attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public ElementsLocalBuilder withElementNamed(
            final String applicationNameOrId,
            final String elementName,
            final Attributes attributes) {
        requireNonNull(applicationNameOrId, "applicationNameOrId");
        requireNonNull(elementName, "aPacakge");
        requireNonNull(attributes, "attributes");
        localElements.add(new ElementsLocalApplicationElementRecord(applicationNameOrId, elementName, attributes));
        return this;
    }

    @Override
    public ElementsLocal build() {

        final var elementClasspath = ClasspathUtils.parse(ELEMENT_CLASSPATH);

        final var factoryRecord = new ElementsLocalFactoryRecord(
                attributes,
                elementClasspath,
                localElements
        );

        final var factory = ServiceLoader
                .load(ElementsLocalFactory.class, getClass().getClassLoader())
                .stream()
                .findFirst()
                .orElseThrow(() -> new SdkException("Unable to find SPI for " + ElementsLocalFactory.class.getName()))
                .get();

        return factory.create(factoryRecord);

    }

}
