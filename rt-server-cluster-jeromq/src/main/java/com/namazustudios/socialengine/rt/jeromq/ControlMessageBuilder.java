package com.namazustudios.socialengine.rt.jeromq;

import com.namazustudios.socialengine.rt.jeromq.CommandPreamble.CommandType;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;

public interface ControlMessageBuilder {
    static ZMsg buildControlMessage(CommandType commandType, ByteBuffer byteBuffer) {
        final CommandPreamble preamble = new CommandPreamble();
        preamble.commandType.set(commandType);

        final ZMsg msg = new ZMsg();

        ByteBuffer preambleByteBuffer = preamble.getByteBuffer();
        byte[] preambleBytes = new byte[preambleByteBuffer.remaining()];
        preambleByteBuffer.get(preambleBytes);

        byte[] commandBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(commandBytes);

        msg.add(new ZFrame(preambleBytes));
        msg.add(new ZFrame(commandBytes));

        return msg;
    }

    // this is deprecated and should be removed entirely
    static void send(final ZMQ.Socket control, CommandPreamble.CommandType commandType, ByteBuffer commandByteBuffer) {
        final CommandPreamble preamble = new CommandPreamble();
        preamble.commandType.set(commandType);

        final ZMsg msg = new ZMsg();

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
