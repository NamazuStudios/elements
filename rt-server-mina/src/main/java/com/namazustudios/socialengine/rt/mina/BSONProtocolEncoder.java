package com.namazustudios.socialengine.rt.mina;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * Created by patricktwohig on 7/26/15.
 */
public class BSONProtocolEncoder implements ProtocolEncoder {

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {

    }

    @Override
    public void dispose(IoSession session) throws Exception {

    }

}
