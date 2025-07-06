package dev.getelements.elements.sdk.local.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.sdk.ElementLoaderFactory;
import dev.getelements.elements.sdk.local.*;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * This is used to load the local elements SDK from Maven on a separate ClassLoader.
 */
public class StandardElementsLocalFactory implements ElementsLocalFactory {

    @Override
    public ElementsLocal create(final ElementsLocalFactoryRecord record) {

        final var classpath = record.classpath().toArray(URL[]::new);
        final var defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        final var injector = Guice.createInjector(
                new JettyServerModule(),
                new ElementsCoreModule(() -> record.attributes().asProperties(defaultConfigurationSupplier.get())),
                new FileSystemElementStorageGitLoaderModule(),
                new ElementsWebServiceComponentModule(),
                new LocalApplicationElementServiceModule(record.elements()),
                new AbstractModule() {
                    @Override
                    protected void configure() {

                        bind(ElementsLocal.class)
                                .to(StandardElementsLocal.class).asEagerSingleton();

                        bind(ElementLoaderFactory.ClassLoaderConstructor.class)
                                .toInstance(parent -> new URLClassLoader(classpath, parent));

                    }
                }
        );

        return injector.getInstance(ElementsLocal.class);

    }

}
