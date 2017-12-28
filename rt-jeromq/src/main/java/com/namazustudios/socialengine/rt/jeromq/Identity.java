package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.Arrays;

public class Identity {

    public static final byte[] EMPTY_DELIMITER = new byte[0];

    public ZMsg popIdentity(final ZMsg msg) {
        final ZMsg identity = new ZMsg();
        while (!msg.isEmpty() && !Arrays.equals(msg.peek().getData(), EMPTY_DELIMITER)) identity.push(msg.pop());
        if (!msg.isEmpty()) msg.pop();
        return identity;
    }

    public void pushIdentity(final ZMsg msg, final ZMsg identity) {
        msg.push(new ZFrame(EMPTY_DELIMITER));
        while(!identity.isEmpty()) msg.push(identity.pop());
    }

}
