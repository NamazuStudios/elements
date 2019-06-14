package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

/**
 * A specialized routing command where we bundle both the invoker and control tcp addresses for a single instance and
 * take care of both connections together. This is necessary because we retrieve the InstanceUUID on the control conn,
 * and we need a way to also relate that InstanceUUID to the invoker conn/socket handle.
 */
public class InstanceConnectionCommand extends Struct {
    public static InstanceConnectionCommand buildInstanceConnectionCommand(
            final Action action,
            final String invokerTcpAddress,
            final String controlTcpAddress
            ) {
        final InstanceConnectionCommand instanceConnectionCommand = new InstanceConnectionCommand();

        if (action != Action.NO_OP && (invokerTcpAddress == null || controlTcpAddress == null)) {
            throw new IllegalArgumentException("Both invoker and tcp addresses must be non-null.");
        }

        instanceConnectionCommand.action.set(action);

        if (invokerTcpAddress != null) {
            instanceConnectionCommand.invokerTcpAddress.set(invokerTcpAddress);
        }

        if (controlTcpAddress != null) {
            instanceConnectionCommand.controlTcpAddress.set(controlTcpAddress);
        }

        return instanceConnectionCommand;
    }

    public static InstanceConnectionCommand InstanceConnectionCommandFromBytes(final byte[] bytes) {
        final InstanceConnectionCommand instanceConnectionCommand = new InstanceConnectionCommand();
        instanceConnectionCommand.getByteBuffer().put(bytes);
        return instanceConnectionCommand;
    }

    public final UTF8String invokerTcpAddress = new UTF8String(64);

    public final UTF8String controlTcpAddress = new UTF8String(64);

    /**
     * The action to perform.
     */
    public final Enum32<Action> action = new Enum32<>(Action.values());

    public enum Action {
        /**
         * Unused for now.
         */
        NO_OP,

        /**
         * Connect to instance. Both the invoker and control tcp addresses must be supplied.
         */
        CONNECT,

        /**
         * Disconnect from instance. Both the invoker and control tcp addresses must be supplied.
         */
        DISCONNECT,
    }
}
