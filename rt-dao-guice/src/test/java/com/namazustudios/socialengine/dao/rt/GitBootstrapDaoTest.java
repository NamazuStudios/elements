package com.namazustudios.socialengine.dao.rt;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Bootstrapper;
import com.namazustudios.socialengine.rt.lua.LuaBootstrapper;
import org.eclipse.jgit.api.Git;
import org.nnsoft.guice.rocoto.converters.FileConverter;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/23/17.
 */
@Guice(modules = GitBootstrapDaoTest.Module.class)
public class GitBootstrapDaoTest {

    private File storageDirectory;

    private GitBootstrapDao gitBootstrapDao;

    @DataProvider
    private Object[][] createRepositoryForMockApplication() throws Exception {

        final UUID uuid = UUID.randomUUID();

        final Application application = new Application();

        application.setId(uuid.toString());
        application.setName("Mock Application");

        final File repositoryDirectory = FilesystemGitLoader.getBareStorageDirectory(getStorageDirectory(), application);
        Assert.assertTrue(repositoryDirectory.mkdirs());

        Git.init()
           .setBare(true)
           .setDirectory(repositoryDirectory)
           .call();

        final User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        return new Object[][]{ { user, application } };

    }

    @Test(dataProvider = "createRepositoryForMockApplication")
    public void testBootstrap(final User user, final Application application) {
        getGitBootstrapDao().bootstrap(user, application);
    }

    @AfterTest
    public void destroyTestDirectory() {
        Files.fileTreeTraverser()
            .postOrderTraversal(getStorageDirectory())
            .filter(f -> f.isFile() || f.isDirectory())
            .forEach(f -> f.delete());
    }

    public File getStorageDirectory() {
        return storageDirectory;
    }

    @Inject
    public void setStorageDirectory(@Named(Constants.GIT_STORAGE_DIRECTORY) File storageDirectory) {
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
                .annotatedWith(Names.named(Constants.GIT_STORAGE_DIRECTORY))
                .to("test-repositores");

            bind(new TypeLiteral<Function<Application, Bootstrapper>>(){}).toInstance(a -> new LuaBootstrapper());

        }
    }
}
