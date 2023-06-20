package dev.getelements.elements.cdnserve;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import dev.getelements.elements.codeserve.ApplicationRepositoryResolver;
import dev.getelements.elements.codeserve.FileSystemApplicationRepositoryResolver;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;

import static dev.getelements.elements.cdnserve.Constants.GIT_CDN_STORAGE_DIRECTORY;

public class CdnServeStorageModule extends AbstractModule {

    @Provides
    @Singleton
    final ApplicationRepositoryResolver buildResolver(
            @Named(GIT_CDN_STORAGE_DIRECTORY) File file,
            final FileSystemApplicationRepositoryResolver fsResolver) {
        fsResolver.initDirectory(file);
        return fsResolver;
    }

}
