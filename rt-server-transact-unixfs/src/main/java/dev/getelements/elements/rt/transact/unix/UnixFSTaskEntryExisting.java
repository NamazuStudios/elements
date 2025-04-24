package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.rt.transact.FatalException;
import dev.getelements.elements.rt.transact.TransactionalTask;
import dev.getelements.elements.sdk.util.FinallyAction;
import dev.getelements.elements.sdk.util.LazyValue;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.FileChannel;
import java.util.*;

import static java.lang.String.format;
import static java.nio.channels.FileChannel.open;
import static java.nio.file.StandardOpenOption.*;

public class UnixFSTaskEntryExisting extends UnixFSTaskEntryBase {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSTaskEntryExisting.class);

    private final UnixFSTaskPathMapping mapping;

    private FinallyAction onClose = FinallyAction.begin(logger);

    private final LazyValue<FileChannel> fileChannel = new SimpleLazyValue<>(this::openFile);

    private FileChannel openFile() {
        return getUnixFSUtils().doOperation(() -> {
            final var fileChannel =  open(mapping.getFilesystemPath(), READ);
            onClose = onClose.then(() -> getUnixFSUtils().doOperationV(fileChannel::close));
            return fileChannel;
        });
    }

    private final LazyValue<UnixFSDataHeader> header = new SimpleLazyValue<>(this::loadHeader);

    private UnixFSDataHeader loadHeader() {
        return getUnixFSUtils().doOperation(() -> UnixFSDataHeader.loadHeader(
                fileChannel.get(),
                UnixFSDataHeader.TASK_MAGIC)
        );
    }

    private final LazyValue<Map<TaskId, TransactionalTask>> originalTasks = new SimpleLazyValue<>(this::loadOriginalTasks);

    private Map<TaskId, TransactionalTask> loadOriginalTasks() {
        return getUnixFSUtils().doOperation(() -> {

            final var header = this.header.get();
            final var fileChannel = this.fileChannel.get();

            final Map<TaskId, TransactionalTask> transactionalTasks = new TreeMap<>();
            fileChannel.position(header.size());

            while (fileChannel.position() < fileChannel.size()) {

                final var task = new UnixFSTask();

                final var byteBuffer = task.getByteBuffer()
                        .position(0)
                        .limit(task.size());

                while (byteBuffer.hasRemaining() && fileChannel.read(byteBuffer) >= 0);

                if (byteBuffer.hasRemaining()) {
                    throw new FatalException("Unexpected end of stream.");
                }

                if (transactionalTasks.put(task.getTaskId(), task) != null) {
                    logger.warn("Duplicate task in file.");
                }

            }

            return transactionalTasks;

        });
    }


    public UnixFSTaskEntryExisting(final OperationalStrategy<ResourceId> operationalStrategy,
                                   final UnixFSUtils unixFSUtils,
                                   final UnixFSTaskPathMapping mapping) {
        super(operationalStrategy, unixFSUtils);
        this.mapping = mapping;
    }

    @Override
    public Optional<ResourceId> findOriginalScope() {
        return Optional.of(header.get().resourceId.get());
    }

    @Override
    public Map<TaskId, TransactionalTask> getOriginalTasksImmutable() {
        return originalTasks.get();
    }

    @Override
    public void close() {
        onClose.close();
    }

}
