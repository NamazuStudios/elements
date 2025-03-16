package dev.getelements.elements.codeserve;

import dev.getelements.elements.git.ApplicationRepositoryResolver;
import dev.getelements.elements.git.FileSystemApplicationRepositoryResolver;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.io.File;

import static dev.getelements.elements.rt.git.FileSystemScriptStorageGitLoaderProvider.ELEMENT_STORAGE_DIRECTORY;

public class FileSystemScriptApplicationRepositoryResolverProvider implements Provider<ApplicationRepositoryResolver> {

    private String scriptStorageDirectory;

    private Provider<FileSystemApplicationRepositoryResolver> fileSystemApplicationRepositoryResolverProvider;

    @Override
    public ApplicationRepositoryResolver get() {
        final var scriptStorageDirectory = getScriptStorageDirectory();
        final var fileSystemApplicationRepositoryResolver = getFileSystemApplicationRepositoryResolverProvider().get();
        fileSystemApplicationRepositoryResolver.initDirectory(new File(scriptStorageDirectory));
        return fileSystemApplicationRepositoryResolver;

    }

    public String getScriptStorageDirectory() {
        return scriptStorageDirectory;
    }

    @Inject
    public void setScriptStorageDirectory(@Named(ELEMENT_STORAGE_DIRECTORY) String scriptStorageDirectory) {
        this.scriptStorageDirectory = scriptStorageDirectory;
    }

    public Provider<FileSystemApplicationRepositoryResolver> getFileSystemApplicationRepositoryResolverProvider() {
        return fileSystemApplicationRepositoryResolverProvider;
    }

    @Inject
    public void setFileSystemApplicationRepositoryResolverProvider(Provider<FileSystemApplicationRepositoryResolver> fileSystemApplicationRepositoryResolverProvider) {
        this.fileSystemApplicationRepositoryResolverProvider = fileSystemApplicationRepositoryResolverProvider;
    }

}

