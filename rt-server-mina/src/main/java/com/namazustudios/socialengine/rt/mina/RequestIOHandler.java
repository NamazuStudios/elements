package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Server;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import javax.inject.Inject;

/**
 * An implementation of {@link IoHandler} which dispatches messages
 * to the {@link Server} implementation.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class RequestIOHandler extends IoHandlerAdapter {

    @Inject
    private Server server;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        final Request request = (Request)message;
        final IoSessionClient ioSessionClient = new IoSessionClient(session);
        server.handleRequest(ioSessionClient, request);
    }

}
