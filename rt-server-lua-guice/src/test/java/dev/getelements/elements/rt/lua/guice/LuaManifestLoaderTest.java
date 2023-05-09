package dev.getelements.elements.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.manifest.event.EventManifest;
import dev.getelements.elements.rt.manifest.http.HttpManifest;
import dev.getelements.elements.rt.manifest.model.ModelManifest;
import dev.getelements.elements.rt.manifest.security.SecurityManifest;
import dev.getelements.elements.rt.manifest.startup.StartupManifest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.client.Client;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNotNull;

/**
 * Created by patricktwohig on 8/17/17.
 */
@Guice(modules = LuaManifestLoaderTest.Module.class)
public class LuaManifestLoaderTest {

    private ManifestLoader manifestLoader;

    @Test()
    public void testLoadAndRun() {
        getManifestLoader().loadAndRunIfNecessary();
    }

    @Test(dependsOnMethods = "testLoadAndRun")
    public void testClose() {
        getManifestLoader().close();
    }

    @Test(dependsOnMethods = "testClose")
    public void testLoadModelManifest() {
        final ModelManifest modelManifest = getManifestLoader().getModelManifest();
        assertNotNull(modelManifest);
    }

    @Test(dependsOnMethods = "testClose")
    public void testLoadHttpManifest() {
        final HttpManifest httpManifest = getManifestLoader().getHttpManifest();
        assertNotNull(httpManifest);
    }

    @Test(dependsOnMethods = "testClose")
    public void testLoadSecurityManifest() {
        final SecurityManifest securityManifest = getManifestLoader().getSecurityManifest();
        assertNotNull(securityManifest);
    }

    @Test(dependsOnMethods = "testClose")
    public void testLoadStartupManifest() {
        final StartupManifest startupManifest = getManifestLoader().getStartupManifest();
        assertNotNull(startupManifest);
    }

    @Test(dependsOnMethods = "testClose")
    public void testLoadEventManifest() {
        final EventManifest eventManifest = getManifestLoader().getEventManifest();
        assertNotNull(eventManifest);
    }

    public ManifestLoader getManifestLoader() {
        return manifestLoader;
    }

    @Inject
    public void setManifestLoader(ManifestLoader manifestLoader) {
        this.manifestLoader = manifestLoader;
    }

    public static class Module extends AbstractModule {

        @Override
        protected void configure() {

            bind(Context.class).toInstance(mock(Context.class));
            bind(Client.class).toInstance(mock(Client.class));

            install(new LuaModule() {
                @Override
                protected void configureFeatures() {
                    enableBasicConverters();
                    enableManifestLoaderFeature();
                }
            });
            install(new JavaEventModule());

        }

        @Provides
        public AssetLoader assetLoader() {
            return new ClasspathAssetLoader();
        }

    }

}
