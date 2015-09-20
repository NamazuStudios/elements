package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.OutgoingNetworkOperations;
import com.namazustudios.socialengine.rt.Request;
import org.apache.mina.core.session.IoSession;

import javax.inject.Inject;

/**
 * The reliable implementation of the {@link OutgoingNetworkOperations} interface.  Because the underlying
 * transport is assumed to be reliable, then this does not implement any sort of timeout mechanism as the
 * underlying transport should provide that.
 *
 * Created by patricktwohig on 9/20/15.
 */
public class ReliableIOSessionOutgoingNetworkOperations implements OutgoingNetworkOperations {

    @Inject
    private IoSession ioSession;

    @Override
    public void dispatch(Request request) {
        ioSession.write(request);
    }

}
