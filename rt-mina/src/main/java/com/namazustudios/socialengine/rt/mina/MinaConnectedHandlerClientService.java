package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.Request;
import com.namazustudios.socialengine.rt.ResponseReceiver;
import com.namazustudios.socialengine.rt.SimpleResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by patricktwohig on 7/27/15.
 */
public class MinaConnectedHandlerClientService {

    private static final Logger logger = LoggerFactory.getLogger(MinaConnectedHandlerClientService.class);

    public ResponseReceiver getResponseReceiver(final IoSessionClientSession minaClient, final Request request) {

        return response -> {

            final SimpleResponse simpleResponse = SimpleResponse.builder()
                    .from(response)
                .build();

            if (request.getHeader().getSequence() != simpleResponse.getResponseHeader().getSequence()) {
                logger.warn("Out of sequence response {} {}", request, response);
            }

            minaClient.getIoSession().write(simpleResponse);

        };

    }

}
