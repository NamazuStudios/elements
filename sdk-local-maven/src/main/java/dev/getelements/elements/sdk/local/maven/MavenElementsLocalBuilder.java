package dev.getelements.elements.sdk.local.maven;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.local.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.sdk.local.maven.Maven.mvn;
import static java.util.Objects.requireNonNull;

public class MavenElementsLocalBuilder implements ElementsLocalBuilder {

    public static final String MAVEN_PHASE;

    public static final String ELEMENT_CLASSPATH;

    public static final String SDK_LOCAL_CLASSPATH;

    public static final String MAVEN_PHASE_ENV = "MAVEN_PHASE";

    public static final String ELEMENT_CLASSPATH_ENV = "SDK_LOCAL_CLASSPATH";

    public static final String SDK_LOCAL_CLASSPATH_ENV = "ELEMENT_LOCAL_CLASSPATH";

    public static final String MAVEN_PHASE_PROPERTY = "dev.getelements.elements.mvn.phase";

    public static final String ELEMENT_CLASSPATH_PROPERTY = "dev.getelements.elements.mvn.element.classpath";

    public static final String SDK_LOCAL_CLASSPATH_PROPERTY = "dev.getelements.elements.mvn.sdk.local.classpath";

    public static final String LOADER_CLASS = "dev.getelements.elements.sdk.local.maven.MavenElementsLocalLoader";

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

        SDK_LOCAL_CLASSPATH = System.getenv(MavenElementsLocalBuilder.SDK_LOCAL_CLASSPATH_ENV) != null
                ? System.getenv(MavenElementsLocalBuilder.SDK_LOCAL_CLASSPATH_ENV)
                : System.getProperty(MavenElementsLocalBuilder.SDK_LOCAL_CLASSPATH_PROPERTY, "target/element-libs/*");

        if (!MAVEN_PHASE.isBlank()) {
            mvn(MAVEN_PHASE);
        }

    }

    private Attributes attributes = Attributes.emptyAttributes();

    private final List<LocalApplicationElementRecord> localElements = new ArrayList<>();

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
        localElements.add(new LocalApplicationElementRecord(applicationNameOrId, elementName, attributes));
        return this;
    }

    @Override
    public ElementsLocal build() {

        final var elementClasspath = ClasspathUtils.parse(ELEMENT_CLASSPATH);
        final var sdkLocalClasspath = ClasspathUtils.parse(SDK_LOCAL_CLASSPATH);
        
        final var sdkClassLoader = new LocalSdkURLClassLoader.Builder()
                .withCoreSdkPackages()
                .withPackage("dev.getelements.elements.sdk.local.maven")
                .build(sdkLocalClasspath);

        try {

            final var loaderClass = sdkClassLoader.loadClass(LOADER_CLASS);

            final var loader = (MavenElementsLocalLoader) loaderClass
                    .getConstructor()
                    .newInstance();

            return loader.load(
                    attributes,
                    elementClasspath,
                    localElements
            );

        } catch (InstantiationException |
                 ClassNotFoundException |
                 InvocationTargetException |
                 IllegalAccessException |
                 NoSuchMethodException ex) {
            throw new SdkException("Unable to load SDK.", ex);
        }

    }

}
