//package com.namazustudios.socialengine.rt.mina;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.namazustudios.socialengine.exception.BaseException;
//import com.namazustudios.socialengine.rt.Path;
//import com.namazustudios.socialengine.rt.*;
//import com.namazustudios.socialengine.rt.handler.ClientRequestHandler;
//import com.namazustudios.socialengine.rt.handler.Handler;
//import com.namazustudios.socialengine.rt.handler.SessionRequestDispatcher;
//import org.apache.mina.core.service.IoHandler;
//import org.apache.mina.core.service.IoHandlerAdapter;
//import org.apache.mina.core.session.IdleStatus;
//import org.apache.mina.core.session.IoSession;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
///**
// * An implementation of {@link IoHandler} which dispatches messages
// * to the {@link SessionRequestDispatcher} implementation.
// *
// * Created by patricktwohig on 7/27/15.
// */
//public class ServerIOHandler extends IoHandlerAdapter {
//
//    private static final Logger LOG = LoggerFactory.getLogger(ServerIOHandler.class);
//
//    private SessionRequestDispatcher sessionRequestDispatcher;
//
//    private MinaConnectedHandlerClientService minaConnectedHandlerClientService;
//
//    private Provider<IoSessionClientSession> ioSessionClientProvider;
//
//    private ObjectMapper objectMapper;
//
//    private ExceptionMapper.Resolver resolver;
//
//    private Container<Handler> handlerContainer;
//
//    @Override
//    public void messageReceived(final IoSession session, final Object message) throws Exception {
//
//        if (message instanceof Request) {
//            handle(session, (Request) message);
//        } else {
//            LOG.error("Received unexpected message from server: {} ", message);
//        }
//
//    }
//
//    private void handle(final IoSession session, final Request request) {
//
//        final IoSessionClientSession ioSessionClient;
//        ioSessionClient = getIoSessionClientProvider().get();
//
//        final ResponseReceiver responseReceiver;
//        responseReceiver = getMinaConnectedHandlerClientService().getResponseReceiver(ioSessionClient, request);
//
//        try {
//
//            Request.Validator.validate(request);
//
//            final Path path = new Path(request.getHeader().getPath());
//
//            getHandlerContainer().performV(path, resource -> {
//
//                final ClientRequestHandler requestHandler;
//                requestHandler = resource.getHandler(request.getHeader().getMethod());
//
//                final Class<?> payloadType = requestHandler.getPayloadType();
//                final SimpleRequest simpleRequest = SimpleRequest.builder().from(request).build();
//                final Object payload = getObjectMapper().convertValue(request.getPayload(), payloadType);
//
//                simpleRequest.setPayload(payload);
//                getSessionRequestDispatcher().dispatch(ioSessionClient, simpleRequest, responseReceiver);
//
//            });
//
//
//        } catch (BaseException ex) {
//            final ExceptionMapper<BaseException> invalidDataExceptionExceptionMapper;
//            invalidDataExceptionExceptionMapper = getResolver().getExceptionMapper(ex);
//            invalidDataExceptionExceptionMapper.map(ex, request, responseReceiver);
//            return;
//        }
//
//    }
//
//    @Override
//    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
//        LOG.error("Caught exception from session.  Closing.");
//        session.close(true);
//    }
//
//    @Override
//    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
//        super.sessionIdle(session, status);
//    }
//
//    @Override
//    public void sessionClosed(IoSession session) throws Exception {
//        super.sessionClosed(session);
//    }
//
//    public SessionRequestDispatcher getSessionRequestDispatcher() {
//        return sessionRequestDispatcher;
//    }
//
//    @Inject
//    public void setSessionRequestDispatcher(SessionRequestDispatcher sessionRequestDispatcher) {
//        this.sessionRequestDispatcher = sessionRequestDispatcher;
//    }
//
//    public MinaConnectedHandlerClientService getMinaConnectedHandlerClientService() {
//        return minaConnectedHandlerClientService;
//    }
//
//    @Inject
//    public void setMinaConnectedHandlerClientService(MinaConnectedHandlerClientService minaConnectedHandlerClientService) {
//        this.minaConnectedHandlerClientService = minaConnectedHandlerClientService;
//    }
//
//    public Provider<IoSessionClientSession> getIoSessionClientProvider() {
//        return ioSessionClientProvider;
//    }
//
//    @Inject
//    public void setIoSessionClientProvider(Provider<IoSessionClientSession> ioSessionClientProvider) {
//        this.ioSessionClientProvider = ioSessionClientProvider;
//    }
//
//    public ObjectMapper getObjectMapper() {
//        return objectMapper;
//    }
//
//    @Inject
//    public void setObjectMapper(ObjectMapper objectMapper) {
//        this.objectMapper = objectMapper;
//    }
//
//    public ExceptionMapper.Resolver getResolver() {
//        return resolver;
//    }
//
//    @Inject
//    public void setResolver(ExceptionMapper.Resolver resolver) {
//        this.resolver = resolver;
//    }
//
//    public Container<Handler> getHandlerContainer() {
//        return handlerContainer;
//    }
//
//    @Inject
//    public void setHandlerContainer(Container<Handler> handlerContainer) {
//        this.handlerContainer = handlerContainer;
//    }
//
//}
