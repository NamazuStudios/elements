package com.namazustudios.socialengine.rt.mina;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.edge.EdgeRequestDispatcher;
import com.namazustudios.socialengine.rt.edge.EdgeRequestPathHandler;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * An implementation of {@link IoHandler} which dispatches messages
 * to the {@link EdgeRequestDispatcher} implementation.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class ServerIOHandler extends IoHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerIOHandler.class);

    @Inject
    private EdgeServer edgeServer;

    @Inject
    private MinaConnectedEdgeClientService minaConnectedEdgeClientService;

    @Inject
    private ResourceService<EdgeResource> edgeResourceService;

    @Inject
    private Provider<IoSessionClient> ioSessionClientProvider;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ExceptionMapper.Resolver resolver;

    @Override
    public void messageReceived(final IoSession session, final Object message) throws Exception {

        if (message instanceof Request) {
            handle(session, (Request) message);
        } else {
            LOG.error("Received unexpected message from server: {} ", message);
        }

    }

    private void handle(final IoSession session, final Request request) {

        final IoSessionClient ioSessionClient = ioSessionClientProvider.get();
        final ResponseReceiver responseReceiver = minaConnectedEdgeClientService.getResponseReceiver(ioSessionClient, request);

        try {
            Request.Validator.validate(request);

            final Path path = new Path(request.getHeader().getPath());

            final EdgeRequestPathHandler edgeRequestPathHandler = edgeResourceService
                    .getResource(path)
                    .getHandler(request.getHeader().getMethod());

            final Class<?> payloadType = edgeRequestPathHandler.getPayloadType();
            final SimpleRequest simpleRequest = SimpleRequest.builder().from(request).build();
            final Object payload = objectMapper.convertValue(request.getPayload(), payloadType);

            simpleRequest.setPayload(payload);
            edgeServer.dispatch(ioSessionClient, simpleRequest, responseReceiver);

        } catch (BaseException ex) {
            final ExceptionMapper<BaseException> invalidDataExceptionExceptionMapper;
            invalidDataExceptionExceptionMapper = resolver.getExceptionMapper(ex);
            invalidDataExceptionExceptionMapper.map(ex, request, responseReceiver);
            return;
        }

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        LOG.error("Caught exception from session.  Closing.");
        session.close(true);
    }

}
