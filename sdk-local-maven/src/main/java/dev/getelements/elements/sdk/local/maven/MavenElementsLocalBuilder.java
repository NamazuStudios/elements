package dev.getelements.elements.sdk.local.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementLoaderFactory.ClassLoaderConstructor;
import dev.getelements.elements.sdk.local.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    private Properties attributes = new Properties();

//    private ClassLoaderConstructor classLoaderConstructor = DelegatingLocalClassLoader::new;

    private final List<LocalApplicationElementRecord> localElements = new ArrayList<>();

    @Override
    public ElementsLocalBuilder withAttributes(final Properties attributes) {
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

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final var injector = Guice.createInjector(
                new JettyServerModule(),
                new ElementsCoreModule(() -> attributes.asProperties(defaultConfigurationSupplier.get())),
                new FileSystemElementStorageGitLoaderModule(),
                new ElementsWebServiceComponentModule(),
                new LocalApplicationElementServiceModule(localElements),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ElementsLocal.class).to(StandardElementsLocal.class).asEagerSingleton();
                        bind(ClassLoaderConstructor.class).toInstance(classLoaderConstructor);
                    }
                }
        );

        return injector.getInstance(ElementsLocal.class);

    }

}
