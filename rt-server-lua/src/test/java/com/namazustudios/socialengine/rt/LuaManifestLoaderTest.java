package com.namazustudios.socialengine.rt;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.model.ModelManifest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static com.namazustudios.socialengine.rt.lua.LuaManifestLoader.MAIN_MANIFEST;
import static org.testng.Assert.assertNotNull;

/**
 * Created by patricktwohig on 8/17/17.
 */
@Test
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

    public ManifestLoader getManifestLoader() {
        return manifestLoader;
    }

    @Inject
    public void setManifestLoader(ManifestLoader manifestLoader) {
        this.manifestLoader = manifestLoader;
    }

    public static class Module extends AbstractModule {

        private final File workingDirectory;

        public Module() throws IOException {

            workingDirectory = Files.createTempDirectory(LuaManifestLoaderTest.class.getSimpleName()).toFile();
            workingDirectory.mkdirs();

            final File manifest = new File(workingDirectory, MAIN_MANIFEST);

            try (final FileOutputStream fos = new FileOutputStream(manifest);
                 final InputStream is = getClass().getResourceAsStream("/" + MAIN_MANIFEST)) {

                int count;
                final byte[] buffer = new byte[1024 * 4];

                while ((count = is.read(buffer)) >= 0) {
                    fos.write(buffer, 0, count);
                }

            }

        }

        @Override
        protected void configure() {
            install(new LuaModule());
        }

        @Provides
        public AssetLoader assetLoader() {
            return new FileAssetLoader(workingDirectory);
        }

    }

}
