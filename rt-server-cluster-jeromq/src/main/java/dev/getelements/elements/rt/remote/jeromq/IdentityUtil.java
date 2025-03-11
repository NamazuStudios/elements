package dev.getelements.elements.rt.remote.jeromq;

import org.zeromq.ZFrame;
import org.zeromq.ZMsg;

import java.util.Arrays;

/**
 * Manipulates the identity frames on an instance of {@link ZMsg}.
 */
public class IdentityUtil {

    public static final byte[] EMPTY_DELIMITER = new byte[0];

    /**
     * Copies the identity portion of the {@link ZMsg}.
     *
     * @param msg the message from which to copy
     * @return a copy of the identity portion in a new {@link ZMsg}
     */
    public static ZMsg copyIdentity(final ZMsg msg) {

        final ZMsg identityMsg = new ZMsg();

        for (final ZFrame frame : msg) {

            if (Arrays.equals(frame.getData(), EMPTY_DELIMITER)) {
                break;
            } else {
                identityMsg.addLast(frame.duplicate());
            }

        }

        return identityMsg;

    }

    /**
     * Removes all frames containing the identity portion from the supplied {@link ZMsg}.  ALl identity frames are moved
     * to a new instance of {@link ZMsg}.  The delimiter frame is discarded.  When this returns, all identity frames
     * as well as the empty delimiter frame are removed from the target message and moved to a new message which is
     * returned by this method.
     *
     * @param msg the message from which to pop the identity
     * @return a new message containing just the identity portion
     */
    public static ZMsg popIdentity(final ZMsg msg) {

        final ZMsg identityMsg = new ZMsg();

        while (!msg.isEmpty() && msg.peek().getData().length != 0) {
            identityMsg.addLast(msg.removeFirst());
        }

        if (!msg.isEmpty()) msg.pop();
        return identityMsg;

    }

    /**
     * Prepends all identity frames to the supplied message.  This always inserts a delimiter frame after the identity
     * portion.  When this returns the identity {@link ZMsg} is empty and all frames moved to the target message.
     *
     * @param msg the message to receive the identity frames
     * @param identity a {@link ZMsg} containing all identity frames
     */
    public static void pushIdentity(final ZMsg msg, final ZMsg identity) {
        msg.push(EMPTY_DELIMITER);
        while (!identity.isEmpty()) msg.addFirst(identity.removeLast());
    }

}
