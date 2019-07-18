package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.remote.CommandPreamble;
import com.namazustudios.socialengine.rt.remote.CommandPreamble.CommandType;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

public class ControlMessageBuilder {

    public static ZMsg buildControlMsg(CommandType commandType, ByteBuffer controlByteBuffer) {
        return buildControlMsg(commandType, Collections.singletonList(controlByteBuffer));
    }

    public static ZMsg buildControlMsg(CommandType commandType, List<ByteBuffer> controlByteBuffers) {
        final CommandPreamble commandPreamble = new CommandPreamble();
        commandPreamble.commandType.set(commandType);

        final ZMsg msg = new ZMsg();

        ByteBuffer preambleByteBuffer = commandPreamble.getByteBuffer();

        preambleByteBuffer.rewind();
        byte[] preambleBytes = new byte[preambleByteBuffer.remaining()];
        preambleByteBuffer.get(preambleBytes);

        msg.add(new ZFrame(preambleBytes));

        controlByteBuffers.forEach(controlByteBuffer -> {
            controlByteBuffer.rewind(); // JF: sometimes the buffer cursor is non-zero, so make sure to explicitly reset
            byte[] controlBytes = new byte[controlByteBuffer.remaining()];
            controlByteBuffer.get(controlBytes);

            msg.add(new ZFrame(controlBytes));
        });

        return msg;
    }

    // this is deprecated and should be removed entirely
    public static void send(final ZMQ.Socket control, CommandPreamble.CommandType commandType, ByteBuffer commandByteBuffer) {
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
