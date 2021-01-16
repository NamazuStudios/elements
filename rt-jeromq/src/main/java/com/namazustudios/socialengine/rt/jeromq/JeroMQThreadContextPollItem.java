package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZMQ;

class JeroMQThreadContextPollItem extends ZMQ.PollItem {

    private final JeroMQAsyncConnection connection;

    public JeroMQThreadContextPollItem(final JeroMQAsyncConnection connection, int ops) {
        super(connection.socket(), ops);
        this.connection = connection;
    }

    public void poll() {
        if (isError()) connection.getOnError().publish(connection);
        if (isReadable()) connection.getOnRead().publish(connection);
        if (isWritable()) connection.getOnWrite().publish(connection);
    }

}
