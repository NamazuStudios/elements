package com.namazustudios.socialengine.dao.rt;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Bootstrapper;
import com.namazustudios.socialengine.rt.lua.LuaBootstrapper;
import org.nnsoft.guice.rocoto.converters.FileConverter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.UUID;
import java.util.function.Function;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;
import static org.testng.Assert.assertTrue;

/**
 * Created by patricktwohig on 8/23/17.
 */
@Test
@Guice(modules = GitBootstrapDaoTest.Module.class)
public class GitBootstrapDaoTest {

    private File storageDirectory;

    private GitBootstrapDao gitBootstrapDao;

    @Test
    public void testBootstrap() {
        final Application mock = new Application();
        mock.setName("mock");
        mock.setId(UUID.randomUUID().toString());
        getGitBootstrapDao().bootstrap(mock);
    }

    @AfterTest
    public void destroyTestDirectory() {
        assertTrue(getStorageDirectory().delete());
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

    @Inject
    public void setStorageDirectory(@Named(GIT_STORAGE_DIRECTORY) File storageDirectory) {
        this.storageDirectory = storageDirectory;
    }

    public GitBootstrapDao getGitBootstrapDao() {
        return gitBootstrapDao;
    }

    @Inject
    public void setGitBootstrapDao(GitBootstrapDao gitBootstrapDao) {
        this.gitBootstrapDao = gitBootstrapDao;
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            install(new FileConverter());
            bind(GitLoader.class).to(FilesystemGitLoader.class);

            bindConstant()
                .annotatedWith(Names.named(GIT_STORAGE_DIRECTORY))
                .to("test-repositores-" + UUID.randomUUID());

            bind(new TypeLiteral<Function<Application, Bootstrapper>>(){}).toInstance(a -> new LuaBootstrapper());

        }
    }
}
