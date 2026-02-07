package dev.getelements.elements.sdk.local.internal;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.jetty.ElementsCoreModule;
import dev.getelements.elements.jetty.ElementsWebServiceComponentModule;
import dev.getelements.elements.jetty.JettyServerModule;
import dev.getelements.elements.rt.git.FileSystemElementStorageGitLoaderModule;
import dev.getelements.elements.sdk.ElementLoaderFactory;
import dev.getelements.elements.sdk.local.ElementsLocal;
import dev.getelements.elements.sdk.local.ElementsLocalFactory;
import dev.getelements.elements.sdk.local.ElementsLocalFactoryRecord;

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

        // TODO: EL-317 Fixes the LocalSDK to use Artifact Resolution instead of local files from disk

        final var injector = Guice.createInjector(
                new JettyServerModule(),
                new ElementsCoreModule(() -> record.attributes().asProperties(defaultConfigurationSupplier.get())),
                new FileSystemElementStorageGitLoaderModule(),
                new ElementsWebServiceComponentModule()
        );

        return injector.getInstance(ElementsLocal.class);

    }

}
