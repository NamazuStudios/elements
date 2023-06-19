package dev.getelements.elements.codeserve;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

import static dev.getelements.elements.rt.git.Constants.GIT_SCRIPT_STORAGE_DIRECTORY;

public class CodeServeStorageModule extends AbstractModule {

    @Provides
    @Singleton
    final ApplicationRepositoryResolver buildResolver(
            @Named(GIT_SCRIPT_STORAGE_DIRECTORY) File file,
            final FileSystemApplicationRepositoryResolver fsResolver) {
        fsResolver.initDirectory(file);
        return fsResolver;
    }

}
