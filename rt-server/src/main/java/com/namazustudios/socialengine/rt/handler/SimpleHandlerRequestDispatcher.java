package com.namazustudios.socialengine.rt.handler;

import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * The simple implementation of the {@link HandlerRequestDispatcher} interface.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class SimpleHandlerRequestDispatcher implements HandlerRequestDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleHandlerRequestDispatcher.class);

    private ExceptionMapper.Resolver exceptionMapperResolver;

    private HandlerFilter.Chain rootFilterChain;

    private Container<Handler> edgeResourceContainer;

    @Override
    public void dispatch(final HandlerClientSession handlerClientSession,
                         final Request request,
                         final ResponseReceiver responseReceiver) {
        try (final DelegatingCheckedResponseReceiver receiver = new DelegatingCheckedResponseReceiver(request, responseReceiver)) {
            executeRootFilterChain(handlerClientSession, request, receiver);
        } catch (Exception ex) {
            LOG.error("Caught exception processing request {}.", request, ex);
        }
    }

    private void executeRootFilterChain(final HandlerClientSession handlerClientSession,
                                        final Request request,
                                        final ResponseReceiver responseReceiver) {
        try {
            rootFilterChain.next(handlerClientSession, request, responseReceiver);
        } catch (Exception ex) {
            mapException(ex, handlerClientSession, request, responseReceiver);
        }
    }

    private <T extends Exception> void mapException(final T ex,
                                                    final HandlerClientSession handlerClientSession,
                                                    final Request request,
                                                    final ResponseReceiver responseReceiver) {
        LOG.info("Mapping exception for request {} and handlerClientSession {}", request, handlerClientSession, ex);
        final ExceptionMapper<T> exceptionMapper = getExceptionMapperResolver().getExceptionMapper(ex);
        exceptionMapper.map(ex, request, responseReceiver);
    }

    @Inject
    public void buildRootFilterChain(final List<HandlerFilter> handlerFilterList) {

        Collections.reverse(handlerFilterList);

        HandlerFilter.Chain chain = (client, request, receiver) -> resolveAndDispatch(client, request, receiver);

        for (final HandlerFilter filter : handlerFilterList) {
            final HandlerFilter.Chain next = chain;
            chain = (edgeClientSession, request, responseReceiver) -> filter.filter(next, edgeClientSession, request, responseReceiver);
        }

        rootFilterChain = chain;

    }

    private void resolveAndDispatch(final HandlerClientSession handlerClientSession,
                                    final Request request,
                                    final ResponseReceiver receiver) {

        final Path path = new Path(request.getHeader().getPath());

        getEdgeResourceContainer().performV(path, resource -> {

            final ClientRequestHandler clientRequestHandler;
            clientRequestHandler = resource.getHandler(request.getHeader().getMethod());

            if (request.getPayload() == null) {
                clientRequestHandler.handle(handlerClientSession, request, receiver);
            } else if (clientRequestHandler.getPayloadType().isAssignableFrom(request.getPayload().getClass())) {
                clientRequestHandler.handle(handlerClientSession, request, receiver);
            } else {
                throw new InvalidParameterException("Method " + request.getHeader().getMethod() + " " +
                        "at path " + request.getHeader().getPath()  + " " +
                        "does not handle payload (" + request.getPayload() + ") " +
                        "of type " + request.getPayload().getClass());
            }

        });

    }

    public Container<Handler> getEdgeResourceContainer() {
        return edgeResourceContainer;
    }

    @Inject
    public void setEdgeResourceContainer(Container<Handler> edgeResourceContainer) {
        this.edgeResourceContainer = edgeResourceContainer;
    }

    public ExceptionMapper.Resolver getExceptionMapperResolver() {
        return exceptionMapperResolver;
    }

    @Inject
    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
        this.exceptionMapperResolver = exceptionMapperResolver;
    }

}
