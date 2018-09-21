package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;

public interface JeroMQSocketHost {
    static void issue(final ZMQ.Socket control, CommandPreamble.CommandType commandType, ByteBuffer commandByteBuffer) {
        final CommandPreamble preamble = new CommandPreamble();
        preamble.commandType.set(commandType);

        ZMsg msg = new ZMsg();

        ByteBuffer preambleByteBuffer = preamble.getByteBuffer();
        byte[] preambleBytes = new byte[preambleByteBuffer.remaining()];
        preambleByteBuffer.get(preambleBytes);

        byte[] commandBytes = new byte[commandByteBuffer.remaining()];
        commandByteBuffer.get(commandBytes);

        msg.add(new ZFrame(preambleBytes));
        msg.add(new ZFrame(commandBytes));

        msg.send(control);
    }
}
