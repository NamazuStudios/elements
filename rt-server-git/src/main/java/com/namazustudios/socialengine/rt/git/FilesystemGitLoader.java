package com.namazustudios.socialengine.rt.git;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.exception.ApplicationCodeNotFoundException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.util.ShutdownHooks;
import com.namazustudios.socialengine.rt.util.TemporaryFiles;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.git.Constants.GIT_STORAGE_DIRECTORY;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.file.Files.*;

/**
 * A special class which will load an {@link ApplicationId}'s code to a local temporary directory where it can be
 * processed and run.
 *
 * The source of the {@link org.eclipse.jgit.lib.Repository} is a path on the file system, and therefore it clones from
 * a git repository stored elsewhere on disk.  This is specified using the {@link Constants#GIT_STORAGE_DIRECTORY}
 * configuration parameter.
 *
 * Note that the {@link GitLoader} interface essentially calls for unpacking of the {@link ApplicationId} code to a
 * local directory.  The designation "Filesystem" refers to the source of the repository as opposed to the id
 * of the working directory.
 *
 * Created by patricktwohig on 8/19/17.
 */
public class FilesystemGitLoader implements GitLoader {

    private static final String GIT_DIRECTORY = ".git";

    private static final Logger logger = LoggerFactory.getLogger(FilesystemGitLoader.class);

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(FilesystemGitLoader.class);

    private static final ShutdownHooks hooks = new ShutdownHooks(FilesystemGitLoader.class);

    private File gitStorageDirectory;

    private final ConcurrentMap<String, Lock> applicationIdLockConcurrentMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, File> applicationIdFileConcurrentMap = new ConcurrentHashMap<>();

    /**
     *
     * Used by the {@link GitLoader} instances to determine the bare storage directory for a
     * particular {@link ApplicationId}.  This derives the path in a unique and consistent
     * manner.
     *
     * @param parent the parent directory as expressed by a {@link File}
     * @param applicationId the {@link ApplicationId}
     *
     * @return a {@link File} representing the bare storage directory for the {@link ApplicationId}
     *
     */
    public static File getBareStorageDirectory(final File parent, final ApplicationId applicationId) {
        final var gitDirectory = new File(parent, format("%s.%s", applicationId.asString(), GIT_SUFFIX)).toPath();
        return gitDirectory.toFile();
    }

    @Override
    public void performInGit(final ApplicationId applicationId,
                             final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {

        final Lock lock = lockFor(applicationId);

        try {
            lock.lock();
            doPerformInGit(applicationId, gitConsumer);
        } finally {
            lock.unlock();
        }

    }

    private void doPerformInGit(final ApplicationId applicationId,
                                final BiConsumer<Git, Function<Path, OutputStream>> gitConsumer) {

        final File codeDirectory = getCodeDirectory(applicationId);

        final Function<Path, OutputStream> stringOutputStreamFunction = path -> {
            try {

                final File file = new File(codeDirectory, path.toFileSystemPathString());
                final File parent = file.getParentFile();

                if (!parent.exists()) {
                    parent.mkdirs();
                }

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
    public File getCodeDirectory(final ApplicationId applicationId) {

        final var lock = lockFor(applicationId);

        try {
            lock.lock();
            return doGetCodeDirectory(applicationId);
        } finally {
            lock.unlock();
        }

    }

    private Lock lockFor(final ApplicationId applicationId) {
        return applicationIdLockConcurrentMap.computeIfAbsent(applicationId.asString(), k -> new ReentrantLock());
    }

    private File doGetCodeDirectory(final ApplicationId applicationId) {
        final File workTree;
        workTree = applicationIdFileConcurrentMap.computeIfAbsent(applicationId.asString(), this::computeWorkTreeDirectory);
        cloneIfNecessary(applicationId, workTree);
        return workTree;
    }

    private File computeWorkTreeDirectory(final String applicationId) {

        final File codeDirectory;

        try {

            final String prefix = format("%s.%s-", applicationId, GIT_SUFFIX);
            codeDirectory = temporaryFiles.createTempDirectory(prefix).toFile().getAbsoluteFile();

            hooks.add(codeDirectory, () -> Files.walk(codeDirectory.toPath())
                 .sorted(Comparator.reverseOrder())
                 .map(java.nio.file.Path::toFile)
                 .forEach(File::delete));

        } catch (UncheckedIOException ex) {
            throw new InternalException(ex.getCause());
        }

        return codeDirectory;

    }

    private void cloneIfNecessary(final ApplicationId applicationId, final File workTree) {

        final FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder()
            .setWorkTree(workTree)
            .setGitDir(new File(workTree, GIT_DIRECTORY))
            .setMustExist(true);

        try (final Repository repository = fileRepositoryBuilder.build()) {
            logger.info("Found {} for application {} at {}.",
                repository,
                applicationId.asString(),
                workTree.getAbsolutePath());
        } catch (RepositoryNotFoundException ex) {
            clone(applicationId, workTree);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    private void clone(final ApplicationId applicationId, final File destinationDirectory) {
        try (final Git git = openCloneCommand(applicationId, destinationDirectory).call()) {

            final List<Ref> branches = git.branchList().call();
            logger.info("Branches available [{}]", join(","), branches);

            if (branches.stream().anyMatch(b -> DEFAULT_MAIN_BRANCH.equals(b.getName()))) {
                git.checkout().setName(DEFAULT_MAIN_BRANCH).call();
                git.submoduleInit();
            }

        } catch (GitAPIException ex) {
            throw new InternalException(ex);
        }
    }

    private CloneCommand openCloneCommand(final ApplicationId applicationId, final File destinationDirectory) {

        final var gitDirectory = getBareStorageDirectory(applicationId);

        if (!gitDirectory.isDirectory()) {
            throw new ApplicationCodeNotFoundException("git directory not found for application: " + applicationId.asString());
        }

        final var prefix = String.format("%s git", applicationId.asString());

        return Git.cloneRepository()
            .setURI(gitDirectory.toURI().toString())
            .setBranch(DEFAULT_MAIN_BRANCH)
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

    private File getBareStorageDirectory(final ApplicationId applicationId) {
        return getBareStorageDirectory(getGitStorageDirectory(), applicationId);
    }

    public File getGitStorageDirectory() {
        return gitStorageDirectory;
    }

    @Inject
    public void setGitStorageDirectory(@Named(GIT_STORAGE_DIRECTORY) File gitStorageDirectory) {
        this.gitStorageDirectory = gitStorageDirectory;
    }

}
