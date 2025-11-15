package dev.getelements.elements.cdnserve;

import dev.getelements.elements.rt.git.FilesystemGitApplicationAssetLoader;
import dev.getelements.elements.rt.git.GitApplicationAssetLoader;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.io.File;

public class FilesystemCdnGitLoaderProvider implements Provider<GitApplicationAssetLoader> {

    /**
     * The storage directory for the git repositories housing the application's script storage.
     */
    @ElementDefaultAttribute(
            value = "cdn-repos/git",
            description = "The storage directory for the git repositories housing the application's CDN assets."
    )
    public static final String GIT_CDN_STORAGE_DIRECTORY = "dev.getelements.elements.git.cdn.storage.directory";

    private String gitStorageDirectory;

    private Provider<FilesystemGitApplicationAssetLoader> filesystemGitLoaderProvider;

    @Override
    public GitApplicationAssetLoader get() {
        final var gitStorageDirectory = getGitStorageDirectory();
        final var filesystemGitLoader = getFilesystemGitLoaderProvider().get();
        filesystemGitLoader.setGitStorageDirectory(new File(gitStorageDirectory));
        return filesystemGitLoader;
    }

    public String getGitStorageDirectory() {
        return gitStorageDirectory;
    }

    @Inject
    public void setGitStorageDirectory(@Named(GIT_CDN_STORAGE_DIRECTORY) String gitStorageDirectory) {
        this.gitStorageDirectory = gitStorageDirectory;
    }

    public Provider<FilesystemGitApplicationAssetLoader> getFilesystemGitLoaderProvider() {
        return filesystemGitLoaderProvider;
    }

    @Inject
    public void setFilesystemGitLoaderProvider(Provider<FilesystemGitApplicationAssetLoader> filesystemGitLoaderProvider) {
        this.filesystemGitLoaderProvider = filesystemGitLoaderProvider;
    }

}
