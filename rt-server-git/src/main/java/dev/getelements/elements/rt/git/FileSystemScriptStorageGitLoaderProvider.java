package dev.getelements.elements.rt.git;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.io.File;

public class FileSystemScriptStorageGitLoaderProvider implements Provider<GitApplicationAssetLoader> {
    /**
     * The storage directory for the git repositories housing the application's script storage.
     */
    @ElementDefaultAttribute(
            value = "script-repos/git",
            description = "The storage directory for the git repositories housing the application's script storage."
    )
    public static final String ELEMENT_STORAGE_DIRECTORY = "dev.getelements.elements.rt.git.element.storage.directory";

    private String directory;

    private Provider<FilesystemGitApplicationAssetLoader> filesystemGitLoaderProvider;

    @Override
    public GitApplicationAssetLoader get() {
        final var gitStorageDirectory = getDirectory();
        final var filesystemGitLoader = getFilesystemGitLoaderProvider().get();
        filesystemGitLoader.setGitStorageDirectory(new File(gitStorageDirectory));
        return filesystemGitLoader;
    }

    public String getDirectory() {
        return directory;
    }

    @Inject
    public void setDirectory(@Named(ELEMENT_STORAGE_DIRECTORY) String directory) {
        this.directory = directory;
    }

    public Provider<FilesystemGitApplicationAssetLoader> getFilesystemGitLoaderProvider() {
        return filesystemGitLoaderProvider;
    }

    @Inject
    public void setFilesystemGitLoaderProvider(Provider<FilesystemGitApplicationAssetLoader> filesystemGitLoaderProvider) {
        this.filesystemGitLoaderProvider = filesystemGitLoaderProvider;
    }

}
