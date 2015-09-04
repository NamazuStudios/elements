package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.edge.EdgeRequestDispatcher;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import javax.inject.Inject;

/**
 * An implementation of {@link IoHandler} which dispatches messages
 * to the {@link EdgeRequestDispatcher} implementation.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class ServerIOHandler extends IoHandlerAdapter {

    @Inject
    private EdgeServer edgeServer;

    @Inject
    private MinaConnectedEdgeClientService minaConnectedEdgeClientService;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        final Request request = (Request)message;
        final IoSessionClient ioSessionClient = new IoSessionClient(session);
        final ResponseReceiver responseReceiver = minaConnectedEdgeClientService
            .getResponseReceiver(ioSessionClient, request);

        edgeServer.dispatch(ioSessionClient, request, responseReceiver);

    }

}
