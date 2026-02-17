package dev.getelements.elements.sdk.local.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.local.ElementsLocalBuilder;
import dev.getelements.elements.sdk.model.system.ElementDeployment;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.local.maven.Maven.POM_XML;
import static dev.getelements.elements.sdk.local.maven.Maven.pomExistsAtPath;
import static dev.getelements.elements.sdk.local.maven.MavenElementsLocal.SOURCE_DIRECTORIES;

public class MavenElementsLocalBuilder implements ElementsLocalBuilder {

    private Attributes attributes = Attributes.emptyAttributes();

    private List<Path> sourceRoots = new ArrayList<>();

    private List<ElementDeployment> deployments = new ArrayList<>();

    @Override
    public ElementsLocalBuilder withSourceRoot(final Path path) {

        if (path == null) {
            throw new IllegalArgumentException("Source path cannot be null");
        } else if (!Files.isDirectory(path)) {
            throw new IllegalArgumentException("Source path is a directory");
        } else if (!pomExistsAtPath(path)) {
            throw new IllegalArgumentException("Source path %s has no %s".formatted(path, POM_XML));
        }

        sourceRoots.add(path);
        return this;

    }

    @Override
    public ElementsLocalBuilder withDeployment(final ElementDeployment elementDeployment) {
        deployments.add(elementDeployment);
        return this;
    }

    @Override
    public ElementsLocalBuilder withAttributes(final Attributes attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public ElementsLocal build() {

        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final var injector = Guice.createInjector(
                new JettyServerModule(),
                new ElementsCoreModule(() -> attributes.asProperties(defaultConfigurationSupplier.get())),
                new ElementsWebServiceComponentModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        final var sourceRootBinder = newSetBinder(binder(), Path.class, named(SOURCE_DIRECTORIES));
                        sourceRoots.forEach(path -> sourceRootBinder.addBinding().toInstance(path));
                    }
                }
        );

        return injector.getInstance(ElementsLocal.class);

    }

}
