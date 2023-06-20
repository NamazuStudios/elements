package dev.getelements.elements.rt.git;

import dev.getelements.elements.rt.Path;
import dev.getelements.elements.rt.id.ApplicationId;
import org.eclipse.jgit.api.Git;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static dev.getelements.elements.rt.git.Constants.GIT_SCRIPT_STORAGE_DIRECTORY;
import static java.lang.String.format;
import static java.lang.String.join;

/**
 * Originally Elements was designed to use only one git repository per application. In an attempt to make this
 * more generic we made this into a wrapper around the standard {@link FilesystemGitLoader}.
 */
public class FilesystemScriptStorageGitLoader implements GitLoader {

    private FilesystemGitLoader delegate;

    @Override
    public void performInGit(final String applicationUniqueName,
                             final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {
        getDelegate().performInGit(applicationUniqueName, gitConsumer);
    }

    @Override
    public void performInGit(final ApplicationId applicationId,
                             final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {
        getDelegate().performInGit(applicationId, gitConsumer);
    }

    @Override
    public File getCodeDirectory(final ApplicationId applicationId) {
        return getDelegate().getCodeDirectory(applicationId);
    }

    public FilesystemGitLoader getDelegate() {
        return delegate;
    }

    @Inject
    public void setDelegate(FilesystemGitLoader delegate) {
        this.delegate = delegate;
    }

    public File getGitStorageDirectory() {
        return getDelegate().getGitStorageDirectory();
    }

    @Inject
    public void setGitStorageDirectory(@Named(GIT_SCRIPT_STORAGE_DIRECTORY) File gitStorageDirectory) {
        getDelegate().setGitStorageDirectory(gitStorageDirectory);
    }

}
