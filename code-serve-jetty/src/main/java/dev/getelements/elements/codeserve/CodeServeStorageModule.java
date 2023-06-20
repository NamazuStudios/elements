package dev.getelements.elements.codeserve;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.getelements.elements.git.ApplicationRepositoryResolver;
import dev.getelements.elements.git.FileSystemApplicationRepositoryResolver;
import dev.getelements.elements.rt.git.FilesystemScriptStorageGitLoaderModule;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

import static dev.getelements.elements.rt.git.Constants.GIT_SCRIPT_STORAGE_DIRECTORY;

public class CodeServeStorageModule extends AbstractModule {

    @Override
    protected void configure() {
        install(new FilesystemScriptStorageGitLoaderModule());
    }

    @Provides
    @Singleton
    final ApplicationRepositoryResolver buildResolver(
            @Named(GIT_SCRIPT_STORAGE_DIRECTORY) File file,
            final FileSystemApplicationRepositoryResolver fsResolver) {
        fsResolver.initDirectory(file);
        return fsResolver;
    }

}
