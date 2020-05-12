package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.id.ResourceId;

import java.nio.file.Path;

public class UnixFSTransactionProgram {

    public static class Builder {

        public Builder unlink(final java.nio.file.Path fsPath) {
            return this;
        }

        public Builder unlink(final com.namazustudios.socialengine.rt.Path rtPath) {
            return this;
        }

        public Builder link(final java.nio.file.Path file, final ResourceId resourceId) {
            return this;
        }

        public Builder link(final java.nio.file.Path fsPath, final com.namazustudios.socialengine.rt.Path rtPath) {
            return this;
        }

        public Builder linkResource(final ResourceId id, final com.namazustudios.socialengine.rt.Path rtPath) {
            return this;
        }

    }

}
