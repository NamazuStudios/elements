package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ClasspathAssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import com.namazustudios.socialengine.rt.manifest.security.SecurityManifest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertNotNull;

/**
 * Created by patricktwohig on 8/17/17.
 */
@Guice(modules = LuaManifestLoaderTest.Module.class)
public class LuaManifestLoaderTest {

    private ManifestLoader manifestLoader;

    @Test
    public void testLoadModelManifest() {
        final ModelManifest modelManifest = getManifestLoader().getModelManifest();
        assertNotNull(modelManifest);
    }

    @Test
    public void testLoadHttpManifest() {
        final HttpManifest httpManifest = getManifestLoader().getHttpManifest();
        assertNotNull(httpManifest);
    }

    @Test
    public void testLoadSecurityManifest() {
        final SecurityManifest securityManifest = getManifestLoader().getSecurityManifest();
        assertNotNull(securityManifest);
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
            install(new LuaModule() {
                @Override
                protected void configureFeatures() {
                    enableBasicConverters();
                    enableManifestLoaderFeature();
                }
            });
        }

        @Provides
        public AssetLoader assetLoader() {
            return new ClasspathAssetLoader();
        }

    }

}
