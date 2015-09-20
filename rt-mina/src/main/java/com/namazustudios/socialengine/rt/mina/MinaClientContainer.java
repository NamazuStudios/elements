package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.ClientContainer;

import java.net.SocketAddress;

/**
 * Created by patricktwohig on 9/20/15.
 */
public class MinaClientContainer implements ClientContainer {

    @Override
    public ConnectedInstance connect(final SocketAddress socketAddress,
                                     final DisconnectHandler... disconnectHandlers) {
        return null;
    }

}
