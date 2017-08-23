package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Path;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.lang.String.format;
import static java.lang.String.join;

/**
 * A special class which will load an {@link Application}'s code to a local temporary directory where
 * it can be processed and run.
 *
 * The source of the {@link org.eclipse.jgit.lib.Repository} is a path on the file system, and therefore
 * it clones from a git repository stored elsewhere on disk.  This is specified using the
 * {@link Constants#GIT_STORAGE_DIRECTORY} configuration parameter.
 *
 * Created by patricktwohig on 8/19/17.
 */
public class FilesystemGitLoader implements GitLoader {

    private static final Logger logger = LoggerFactory.getLogger(FilesystemGitLoader.class);

    private File gitStorageDirectory;

    private final ConcurrentMap<String, Lock> applicationIdLockConcurrentMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, File> applicationIdFileConcurrentMap = new ConcurrentHashMap<>();

    @Override
    public void performInGit(final Application application,
                             final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {

        final Lock lock = lockFor(application);

        try {
            lock.lock();
            doPerformInGit(application, gitConsumer);
        } finally {
            lock.unlock();
        }

    }

    private void doPerformInGit(final Application application,
                                final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {

        final File codeDirectory = getCodeDirectory(application);

        final Function<Path, OutputStream> stringOutputStreamFunction = path -> {
            try {
                final File file = new File(codeDirectory, path.toFileSystemPathString());
                return new FileOutputStream(file);
            } catch (IOException ex) {
                throw new InternalException(ex);
            }
        };

        try (final Git git = Git.open(codeDirectory)) {
            gitConsumer.accept(git, stringOutputStreamFunction);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }


    @Override
    public File getCodeDirectory(final Application application) {

        final Lock lock = lockFor(application);

        try {
            lock.lock();
            return doGetCodeDirectory(application);
        } finally {
            lock.unlock();
        }

    }

    private Lock lockFor(final Application application) {
        return applicationIdLockConcurrentMap.computeIfAbsent(application.getId(), k -> new ReentrantLock());
    }

    private File doGetCodeDirectory(final Application application) {
        final File codeDirectory;
        codeDirectory = applicationIdFileConcurrentMap.computeIfAbsent(application.getId(), k -> computeCodeDirectory(k));
        cloneIfNecessary(application, codeDirectory);
        return codeDirectory;
    }

    private File computeCodeDirectory(final String applicationId) {

        final File codeDirectory;

        try {
            final String prefix = format("%s.%s", applicationId, GIT_SUFFIX);
            codeDirectory = Files.createTempDirectory(prefix).toFile();
            codeDirectory.deleteOnExit();
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

        return codeDirectory;

    }

    private void cloneIfNecessary(final Application application, final File codeDirectory) {

        try (final FileRepository repository = new FileRepository(codeDirectory)) {

            repository.incrementOpen();

            if (!repository.getConfig().getFile().exists()) {
                clone(application, codeDirectory);
            }

        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    private void clone(final Application application, final File destinationDirectory) {
        try (final Git git = openCloneCommand(application, destinationDirectory).call()) {
            git.checkout().setName(MAIN_BRANCH).call();
            git.submoduleInit();
        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    private CloneCommand openCloneCommand(final Application application, final File destinationDirectory) {

        final File gitDirectory = getBareStorageDirectory(application);

        if (!gitDirectory.isDirectory()) {
            throw new NotFoundException("git directory not found for application: " + application.getId());
        }

        final String prefix = format("%s (%s) git", application.getName(), application.getId());

        return Git.cloneRepository()
            .setURI(gitDirectory.toURI().toString())
            .setBranch(MAIN_BRANCH)
            .setDirectory(destinationDirectory)
            .setCloneSubmodules(true)
            .setCloneAllBranches(true)
            .setCallback(new CloneCommand.Callback() {

                @Override
                public void initializedSubmodules(final Collection<String> submodules) {
                    logger.info("{} initialized submodule - [{}]", prefix, join(",", submodules));
                }

                @Override
                public void cloningSubmodule(final String path) {
                    logger.info("{} cloned submodule - {}", prefix, path);
                }

                @Override
                public void checkingOut(final AnyObjectId commit, final String path) {
                    logger.info("{} checked out - {}@{}", prefix, path, commit.name());
                }

            });

    }

    private File getBareStorageDirectory(final Application application) {
        final String directoryName = format("%s.%s", application.getId(), GIT_SUFFIX);
        final File gitDirectory = new File(getGitStorageDirectory(), directoryName);
        return gitDirectory.getAbsoluteFile();
    }

    public File getGitStorageDirectory() {
        return gitStorageDirectory;
    }

    @Inject
    public void setGitStorageDirectory(@Named(Constants.GIT_STORAGE_DIRECTORY) File gitStorageDirectory) {
        this.gitStorageDirectory = gitStorageDirectory;
    }

}
