package dev.getelements.elements.cdnserve;

import dev.getelements.elements.git.ApplicationRepositoryResolver;
import dev.getelements.elements.git.FileSystemApplicationRepositoryResolver;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.io.File;

import static dev.getelements.elements.cdnserve.FilesystemCdnGitLoaderProvider.GIT_CDN_STORAGE_DIRECTORY;

public class FilesystemCdnApplicationRepositoryResolverProvider implements Provider<ApplicationRepositoryResolver> {

    private String cdnStorageDirectory;

    private Provider<FileSystemApplicationRepositoryResolver> fileSystemApplicationRepositoryResolverProvider;

    @Override
    public ApplicationRepositoryResolver get() {
        final var cdnStorageDirectory = getCdnStorageDirectory();
        final var fileSystemApplicationRepositoryResolver = getFileSystemApplicationRepositoryResolverProvider().get();
        fileSystemApplicationRepositoryResolver.initDirectory(new File(cdnStorageDirectory));
        return fileSystemApplicationRepositoryResolver;
    }

    public String getCdnStorageDirectory() {
        return cdnStorageDirectory;
    }

    @Inject
    public void setCdnStorageDirectory(@Named(GIT_CDN_STORAGE_DIRECTORY) String cdnStorageDirectory) {
        this.cdnStorageDirectory = cdnStorageDirectory;
    }

    public Provider<FileSystemApplicationRepositoryResolver> getFileSystemApplicationRepositoryResolverProvider() {
        return fileSystemApplicationRepositoryResolverProvider;
    }

    @Inject
    public void setFileSystemApplicationRepositoryResolverProvider(Provider<FileSystemApplicationRepositoryResolver> fileSystemApplicationRepositoryResolverProvider) {
        this.fileSystemApplicationRepositoryResolverProvider = fileSystemApplicationRepositoryResolverProvider;
    }

}
