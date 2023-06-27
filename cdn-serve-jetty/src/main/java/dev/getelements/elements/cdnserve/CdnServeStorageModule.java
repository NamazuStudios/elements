package dev.getelements.elements.cdnserve;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.getelements.elements.git.ApplicationRepositoryResolver;
import dev.getelements.elements.git.FileSystemApplicationRepositoryResolver;
import dev.getelements.elements.rt.git.FilesystemGitLoader;
import dev.getelements.elements.rt.git.GitLoader;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

import static dev.getelements.elements.cdnserve.Constants.GIT_CDN_STORAGE_DIRECTORY;

public class CdnServeStorageModule extends AbstractModule {

    @Provides
    @Singleton
    final GitLoader buildGitLoader(
            @Named(GIT_CDN_STORAGE_DIRECTORY) File gitStorageDirectory,
            final FilesystemGitLoader fsResolver) {
        fsResolver.setGitStorageDirectory(gitStorageDirectory);
        return fsResolver;
    }

    @Provides
    @Singleton
    final ApplicationRepositoryResolver buildResolver(
            @Named(GIT_CDN_STORAGE_DIRECTORY) File gitStorageDirectory,
            final FileSystemApplicationRepositoryResolver fsResolver) {
        fsResolver.initDirectory(gitStorageDirectory);
        return fsResolver;
    }

}
