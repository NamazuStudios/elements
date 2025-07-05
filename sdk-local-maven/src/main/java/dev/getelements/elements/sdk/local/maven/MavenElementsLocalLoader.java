package dev.getelements.elements.sdk.local.maven;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementLoaderFactory;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.local.LocalApplicationElementRecord;
import dev.getelements.elements.sdk.local.LocalApplicationElementServiceModule;
import dev.getelements.elements.sdk.local.StandardElementsLocal;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * This is used to load the local elements SDK from Maven on a separate ClassLoader.
 */
public class MavenElementsLocalLoader {

    public ElementsLocal load(
            final Attributes attributes,
            final List<URL> localElementClassPath,
            final List<LocalApplicationElementRecord> localElements
    ) {

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
                        bind(ElementLoaderFactory.ClassLoaderConstructor.class)
                                .toInstance(parent -> new URLClassLoader(
                                        "Elements Local",
                                        localElementClassPath.toArray(URL[]::new),
                                        parent
                                ));
                    }
                }
        );

        return injector.getInstance(ElementsLocal.class);

    }

}
