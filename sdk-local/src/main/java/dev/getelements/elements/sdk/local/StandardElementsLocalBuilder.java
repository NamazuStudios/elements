package dev.getelements.elements.sdk.local;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.guice.GuiceServiceLocator;

import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.sdk.Attributes.emptyAttributes;
import static java.util.Objects.requireNonNull;

class StandardElementsLocalBuilder implements ElementsLocalBuilder {

    private Attributes attributes = emptyAttributes();

    private final List<LocalApplicationElementRecord> localElements = new ArrayList<>();

    @Override
    public ElementsLocalBuilder withAttributes(
            final Attributes attributes) {
        this.attributes = attributes == null ? emptyAttributes() : attributes;
        return this;
    }

    @Override
    public ElementsLocalBuilder withElementFromPacakge(
            final String applicationNameOrId,
            final String aPacakge,
            final Attributes attributes) {
        requireNonNull(applicationNameOrId, "applicationNameOrId");
        requireNonNull(aPacakge, "aPacakge");
        requireNonNull(attributes, "attributes");
        localElements.add(new LocalApplicationElementRecord(applicationNameOrId, aPacakge, attributes));
        return this;
    }

    @Override
    public ElementsLocal build() {

        final var injector = Guice.createInjector(
                new JettyServerModule(),
                new ElementsCoreModule(() -> attributes.asProperties()),
                new FileSystemElementStorageGitLoaderModule(),
                new ElementsWebServiceComponentModule(),
                new LocalApplicationElementServiceModule(localElements),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(ServiceLocator.class).to(GuiceServiceLocator.class);
                        bind(ElementsLocal.class).to(StandardElementsLocal.class);
                    }
                }
        );

        return injector.getInstance(ElementsLocal.class);

    }

}
