package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

public enum JeroMQControlResponseCode {

    /**
     * Indicates that the response was okay.
     */
    OK,

    /**
     * Indicates that the client issued an unknown command.
     */
    UNKNOWN_COMMAND,

    /**
     * Indicates there was no such route for the given {@link NodeId}
     */
    NO_SUCH_NODE,

    /**
     * Indicates there was no such route for the given {@link InstanceId}
     */
    NO_SUCH_INSTANCE,

    /**
     * Indicates a binding already exists.
     */
    BINDING_ALREADY_EXISTS,

    /**
     * Indicates a socket error occurred, such as network timeout.
     */
    SOCKET_ERROR,

    /**
     * There was an exception processing the request.
     */
    EXCEPTION,

    /**
     * Indicates that there was a protocol error.  For example, the server sent malformed data that the client
     * cannot understand.
     */
    PROTOCOL_ERROR,

    /**
     * Indicates an unknown error.
     */
    UNKNOWN_ERROR;

    private static final JeroMQControlResponseCode VALUES[] = values();

    /**
     * Pushes the code as the first frame in the specified {@link ZMsg}.
     *
     * @param zMsg the {@link ZMsg} to receive the command
     */
    public void pushResponseCode(final ZMsg zMsg) {

        final byte data[] = new byte[Integer.BYTES];

        int index = 0;
        final int ordinal = ordinal();

        data[index++] = (byte) (ordinal >> (4 * 3));
        data[index++] = (byte) (ordinal >> (4 * 2));
        data[index++] = (byte) (ordinal >> (4 * 1));
        data[index++] = (byte) (ordinal >> (4 * 0));

        final ZFrame frame = new ZFrame(data);
        zMsg.addFirst(frame);

    }

    /**
     * Gets the {@link JeroMQControlResponseCode} or throw an {@link IllegalArgumentException} if the code could not
     * be understood.  This removes the first frame of the message allowing subsequent processing to take place.
     *
     * @param zMsg the message from which to read the command.
     *
     * @return the {@link JeroMQControlResponseCode}
     */
    public static JeroMQControlResponseCode stripCode(final ZMsg zMsg) {
        if (zMsg.isEmpty()) throw new IllegalArgumentException("Missing response header frame.");
        final ZFrame frame = zMsg.removeFirst();
        return readCode(frame);
    }

    /**
     * Gets the {@link JeroMQControlResponseCode} or throw an {@link IllegalArgumentException} if the code could not
     * be understood.  This reads the first frame of the message allowing subsequent processing to take place.
     *
     * @param zMsg the message from which to read the command.
     *
     * @return the {@link JeroMQControlResponseCode}
     */
    public static JeroMQControlResponseCode readCode(final ZMsg zMsg) {
        if (zMsg.isEmpty()) throw new IllegalArgumentException("Missing response header frame.");
        final ZFrame frame = zMsg.getFirst();
        return readCode(frame);
    }

    /**
     * Gets the {@link JeroMQControlResponseCode} or throw an {@link IllegalArgumentException} if the code could not
     * be understood.  This reads the first frame of the message allowing subsequent processing to take place.
     *
     * @param frame the frame containin the code
     * @return the {@link JeroMQControlResponseCode}
     */
    public static JeroMQControlResponseCode readCode(final ZFrame frame) {

        final byte[] data = frame.getData();

        if (data.length != Integer.BYTES) throw new IllegalArgumentException("Invalid byte array size: " + data);

        try {

            int i = 0;
            int ordinal = 0;
            ordinal |= (data[i++] << (4 * 3));
            ordinal |= (data[i++] << (4 * 2));
            ordinal |= (data[i++] << (4 * 1));
            ordinal |= (data[i++] << (4 * 0));

            return VALUES[ordinal];
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalArgumentException(ex);
        }

    }
}
