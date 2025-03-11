package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.rt.transact.TransactionalTask;
import javolution.io.Struct;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

import static dev.getelements.elements.sdk.cluster.id.TaskId.getSizeInBytes;

public class UnixFSTask extends Struct implements TransactionalTask {

    final PackedTaskId packedTaskId = new PackedTaskId();

    final Signed64 timestamp = new Signed64();

    class PackedTaskId extends Member {

        public PackedTaskId() {
            super(getSizeInBytes() * Byte.SIZE, 1);
        }

        public TaskId get() {
            return TaskId.taskIdFromByteBuffer(
                    getByteBuffer(),
                    getByteBufferPosition() + offset()
            );
        }

        public void set(final TaskId taskId) {
            taskId.toByteBuffer(getByteBuffer(), getByteBufferPosition() + offset());
        }

        @Override
        public String toString() {
            try {
                return get().toString();
            } catch (Exception ex) {
                return "<undefined>";
            }
        }

    }

    public void write(final WritableByteChannel wbc) throws IOException {

        final var position = getByteBufferPosition();

        final var buffer = getByteBuffer()
                .position(position)
                .limit(position + size());

        while (buffer.hasRemaining()) {
            if (wbc.write(buffer) < 0) {
                throw new IOException("Unexpected end of stream.");
            }
        }

    }

    @Override
    public TaskId getTaskId() {
        return packedTaskId.get();
    }

    @Override
    public long getTimestamp() {
        return timestamp.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnixFSTask that = (UnixFSTask) o;
        return Objects.equals(getTaskId(), that.getTaskId()) && Objects.equals(getTimestamp(), that.getTimestamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTaskId(), getTimestamp());
    }

}
