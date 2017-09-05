//package com.namazustudios.socialengine.rt.handler;
//
//import com.namazustudios.socialengine.rt.*;
//import com.namazustudios.socialengine.rt.exception.BadRequestException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Inject;
//import java.util.Collections;
//import java.util.List;
//
///**
// * The simple implementation of the {@link SessionRequestDispatcher} interface.
// *
// * Created by patricktwohig on 7/27/15.
// */
//public class SimpleSessionRequestDispatcher implements SessionRequestDispatcher {
//
//    private static final Logger LOG = LoggerFactory.getLogger(SimpleSessionRequestDispatcher.class);
//
//    private ExceptionMapper.Resolver exceptionMapperResolver;
//
//    private Filter.Chain rootFilterChain;
//
//    private Scheduler<Handler> edgeResourceContainer;
//
//    @Override
//    public void dispatch(final Session session,
//                         final Request request,
//                         final ResponseReceiver responseReceiver) {
//        try (final DelegatingCheckedResponseReceiver receiver = new DelegatingCheckedResponseReceiver(request, responseReceiver)) {
//            executeRootFilterChain(session, request, receiver);
//        } catch (Exception ex) {
//            LOG.error("Caught exception processing request {}.", request, ex);
//        }
//    }
//
//    private void executeRootFilterChain(final Session session,
//                                        final Request request,
//                                        final ResponseReceiver responseReceiver) {
//        try {
//            rootFilterChain.next(session, request, responseReceiver);
//        } catch (Exception ex) {
//            mapException(ex, session, request, responseReceiver);
//        }
//    }
//
//    private <T extends Exception> void mapException(final T ex,
//                                                    final Session session,
//                                                    final Request request,
//                                                    final ResponseReceiver responseReceiver) {
//        LOG.info("Mapping exception for request {} and session {}", request, session, ex);
//        final ExceptionMapper<T> exceptionMapper = getExceptionMapperResolver().getExceptionMapper(ex);
//        exceptionMapper.map(ex, request, responseReceiver);
//    }
//
//    @Inject
//    public void buildRootFilterChain(final List<Filter> filterList) {
//
//        Collections.reverse(filterList);
//
//        Filter.Chain chain = (client, request, receiver) -> resolveAndDispatch(client, request, receiver);
//
//        for (final Filter filter : filterList) {
//            final Filter.Chain next = chain;
//            chain = (edgeClientSession, request, responseReceiver) -> filter.filter(next, edgeClientSession, request, responseReceiver);
//        }
//
//        rootFilterChain = chain;
//
//    }
//
//    private void resolveAndDispatch(final Session session,
//                                    final Request request,
//                                    final ResponseReceiver receiver) {
//
//        final Path path = new Path(request.getHeader().getPath());
//
//        getEdgeResourceContainer().performV(path, resource -> {
//
//            final ClientRequestHandler clientRequestHandler;
//            clientRequestHandler = resource.getHandler(request.getHeader().getMethod());
//
//            if (request.getPayload() == null) {
//                clientRequestHandler.handle(session, request, receiver);
//            } else if (clientRequestHandler.getPayloadType().isAssignableFrom(request.getPayload().getClass())) {
//                clientRequestHandler.handle(session, request, receiver);
//            } else {
//                throw new BadRequestException("Method " + request.getHeader().getMethod() + " " +
//                        "at path " + request.getHeader().getPath()  + " " +
//                        "does not handle payload (" + request.getPayload() + ") " +
//                        "of type " + request.getPayload().getClass());
//            }
//
//        });
//
//    }
//
//    public Scheduler<Handler> getEdgeResourceContainer() {
//        return edgeResourceContainer;
//    }
//
//    @Inject
//    public void setEdgeResourceContainer(Scheduler<Handler> edgeResourceContainer) {
//        this.edgeResourceContainer = edgeResourceContainer;
//    }
//
//    public ExceptionMapper.Resolver getExceptionMapperResolver() {
//        return exceptionMapperResolver;
//    }
//
//    @Inject
//    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
//        this.exceptionMapperResolver = exceptionMapperResolver;
//    }
//
//}
