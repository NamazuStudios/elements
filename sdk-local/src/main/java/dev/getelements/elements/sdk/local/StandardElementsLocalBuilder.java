package dev.getelements.elements.sdk.local;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementLoaderFactory.ClassLoaderConstructor;

import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;
import static java.util.Objects.requireNonNull;

public class StandardElementsLocalBuilder implements ElementsLocalBuilder {

    private Attributes attributes = emptyAttributes();

    private ClassLoaderConstructor classLoaderConstructor = DelegatingLocalClassLoader::new;

    private final List<LocalApplicationElementRecord> localElements = new ArrayList<>();

    @Override
    public ElementsLocalBuilder withAttributes(
            final Attributes attributes) {
        this.attributes = attributes == null ? emptyAttributes() : attributes;
        return this;
    }

    @Override
    public ElementsLocalBuilder withClassLoaderConstructor(final ClassLoaderConstructor constructor) {
        this.classLoaderConstructor = constructor == null ? DelegatingLocalClassLoader::new : constructor;
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
