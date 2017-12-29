package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZMsg;

import java.util.Arrays;

public class Identity {

    public static final byte[] EMPTY_DELIMITER = new byte[0];

    public ZMsg popIdentity(final ZMsg msg) {

        final ZMsg identity = new ZMsg();

        while (!msg.isEmpty() && !Arrays.equals(msg.peek().getData(), EMPTY_DELIMITER)) {
            identity.addLast(msg.removeFirst());
        }

        if (!msg.isEmpty()) msg.pop();
        return identity;

    }

    public void pushIdentity(final ZMsg msg, final ZMsg identity) {

        msg.push(EMPTY_DELIMITER);

        while (!identity.isEmpty()) {
            identity.addFirst(identity.removeLast());
        }

    }

}
