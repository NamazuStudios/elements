package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.Server;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class RequestIOHandler extends IoHandlerAdapter {

    @Inject
    private Server server;

    @Inject
    private MinaConnectedClientService minaConnectedClientService;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {

        final Request request = (Request)message;

        try {

        } catch (BaseException ex) {
            // TODO Respond with error response
        } catch (Exception ex) {
            // TODO Respond with internal error response
        }

    }

}
