package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.id.ResourceId;

public class UnixFSTransactionProgram {

    public void commit() {
        // TODO Implement This.
    }

    public static class Builder {

        public Builder unlinkFile(final UnixFSTransactionCommand.Phase phase,
                                  final java.nio.file.Path fsPath) {
            return this;
        }

        public Builder unlinkResource(final UnixFSTransactionCommand.Phase phase,
                                      final com.namazustudios.socialengine.rt.Path rtPath) {
            return this;
        }

        public Builder linkFile(final UnixFSTransactionCommand.Phase phase,
                                final java.nio.file.Path file,
                                final ResourceId resourceId) {
            return this;
        }

        public Builder linkFile(final UnixFSTransactionCommand.Phase phase,
                                final java.nio.file.Path fsPath,
                                final com.namazustudios.socialengine.rt.Path rtPath) {
            return this;
        }

        public Builder linkResource(final UnixFSTransactionCommand.Phase phase,
                                    final ResourceId id,
                                    final com.namazustudios.socialengine.rt.Path rtPath) {
            return this;
        }

        public UnixFSTransactionProgram compile() {
            // TODO Implement Program Compilation.
            return null;
        }

        public Builder removeResource(final UnixFSTransactionCommand.Phase phase, final ResourceId resourceId) {
            return this;
        }

        public Builder deletePath(final UnixFSTransactionCommand.Phase phase, final Path path) {
            return this;
        }

    }

}
