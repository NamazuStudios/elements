package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.git.FilesystemGitLoader;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.ServiceMayNotContinueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.git.FilesystemGitLoader.getBareStorageDirectory;
import static com.namazustudios.socialengine.rt.git.FilesystemGitLoader.getLegacyDirectory;
import static java.nio.file.Files.createSymbolicLink;

/**
 * This loads an instance of {@link Repository} from a place on the filesystem.
 *
 * Created by patricktwohig on 8/2/17.
 */
public class FileSystemApplicationRepositoryResolver implements ApplicationRepositoryResolver {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemApplicationRepositoryResolver.class);

    private File gitStorageDirectory;

    @Override
    public Repository resolve(final Application application, Consumer<Repository> onCreate) throws Exception {

        final File repositoryDirectory = getStorageDirectoryForApplication(application);
        final FileRepository fileRepository = new FileRepository(repositoryDirectory);

        if (!fileRepository.getConfig().getFile().exists()) {
            fileRepository.create(true);
            onCreate.accept(fileRepository);
        }

        fileRepository.incrementOpen();
        return fileRepository;

    }

    private final File getStorageDirectoryForApplication(final Application application) throws ServiceMayNotContinueException {

        final var applicationId = ApplicationId.forUniqueName(application.getId());

        final var repositoryDirectory = getBareStorageDirectory(
            getGitStorageDirectory(),
            applicationId,
            dir -> getLegacyDirectory(getGitStorageDirectory(), application.getId())
        );

        if (!repositoryDirectory.exists() && !repositoryDirectory.mkdirs()) {
            throw new ServiceMayNotContinueException("cannot create " + repositoryDirectory.getAbsolutePath());
        } else if (!repositoryDirectory.isDirectory()) {
            throw new ServiceMayNotContinueException(repositoryDirectory.getAbsolutePath() + " is not a directory.");
        }

        return repositoryDirectory;

    }

    public File getGitStorageDirectory() {
        return gitStorageDirectory;
    }

    @Inject
    public void setGitStorageDirectory(@Named(Constants.GIT_STORAGE_DIRECTORY) File gitStorageDirectory) {
        this.gitStorageDirectory = gitStorageDirectory;
    }

}
