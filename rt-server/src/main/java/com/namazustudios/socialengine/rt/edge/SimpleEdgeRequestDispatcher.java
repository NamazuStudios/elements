package com.namazustudios.socialengine.rt.edge;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The simple implementation of the {@link EdgeRequestDispatcher} interface.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class SimpleEdgeRequestDispatcher implements EdgeRequestDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEdgeRequestDispatcher.class);

    private static final Map<ErrorCode, ResponseCode> RESPONSE_STATUS_MAP = Maps.immutableEnumMap(
        new ImmutableMap.Builder<ErrorCode, ResponseCode>()
            .put(ErrorCode.DUPLICATE, ResponseCode.BAD_REQUEST_FATAL)
            .put(ErrorCode.FORBIDDEN, ResponseCode.FAILED_AUTH_FATAL)
            .put(ErrorCode.INVALID_DATA, ResponseCode.BAD_REQUEST_FATAL)
            .put(ErrorCode.NOT_FOUND, ResponseCode.PATH_NOT_FOUND)
            .put(ErrorCode.OVERLOAD, ResponseCode.TOO_BUSY_FATAL)
            .put(ErrorCode.INVALID_PARAMETER, ResponseCode.BAD_REQUEST_FATAL)
            .put(ErrorCode.UNKNOWN, ResponseCode.INTERNAL_ERROR_FATAL)
        .build());

    @Inject
    private ResourceService<EdgeResource> resourceService;

    @Inject
    private ExceptionMapper.Resolver exceptionMapperResolver;

    private EdgeFilter.Chain rootFilterChain;

    @Override
    public void dispatch(final Client client, final Request request, final EdgeResponseReceiver edgeResponseReceiver) {
        try (final DelegatingCheckedReceiver receiver = new DelegatingCheckedReceiver(client, request, edgeResponseReceiver)) {
            executeRootFilterChain(client, request, receiver);
        } catch (Exception ex) {
            LOG.error("Caught exception processing request {}.", request, ex);
        }
    }

    private void executeRootFilterChain(final Client client,
                                        final Request request,
                                        final EdgeResponseReceiver edgeResponseReceiver) {
        try {
            rootFilterChain.next(client, request, edgeResponseReceiver);
        } catch (Exception ex) {
            mapException(ex, client, request, edgeResponseReceiver);
        }
    }

    private <T extends Exception> void mapException(final T ex,
                                                    final Client client,
                                                    final Request request,
                                                    final EdgeResponseReceiver edgeResponseReceiver) {

        try {

            LOG.info("Mapping exception for request {} and client {}", request, client, ex);

            final ExceptionMapper<T> exceptionMapper = exceptionMapperResolver.getExceptionMapper(ex);

            if (exceptionMapper == null) {
                mapUnhandled(ex, client, request, edgeResponseReceiver);
            } else {
                exceptionMapper.map(ex, client, request, edgeResponseReceiver);
            }

        } catch (Exception _ex) {
            LOG.error("Caught exception attempting to forumulate exception response from request {} for client {} ", request, client, _ex);
            mapUnhandled(_ex, client, request, edgeResponseReceiver);
        }

    }

    private <T extends Exception> void mapUnhandled(final T ex,
                                                    final Client client,
                                                    final Request request,
                                                    final EdgeResponseReceiver edgeResponseReceiver) {

        final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
        simpleExceptionResponsePayload.setMessage(ex.getMessage());

        ResponseCode code;

        try {
            throw ex;
        } catch (BaseException bex) {
            code = RESPONSE_STATUS_MAP.get(bex.getCode());
            code = code == null ? ResponseCode.INTERNAL_ERROR_FATAL : code;
            LOG.warn("Caught exception handling request {} to client {}.", request, client, bex);
        } catch (Exception e) {
            code = ResponseCode.INTERNAL_ERROR_FATAL;
            LOG.error("Caught exception handling request {} to client {}.", request, client, e);
        }

        edgeResponseReceiver.receive(code.getCode(), simpleExceptionResponsePayload);

    }

    @Inject
    public void buildRootFilterChain(final List<EdgeFilter> edgeFilterList) {

        Collections.reverse(edgeFilterList);

        EdgeFilter.Chain chain = new EdgeFilter.Chain() {
            @Override
            public void next(final Client client,
                             final Request request,
                             final EdgeResponseReceiver receiver) {
                resolveAndDispatch(client, request, receiver);
            }
        };

        for (final EdgeFilter filter : edgeFilterList) {

            final EdgeFilter.Chain next = chain;

            chain = new EdgeFilter.Chain() {
                @Override
                public void next(final Client client,
                                 final Request request,
                                 final EdgeResponseReceiver edgeResponseReceiver) {
                    filter.filter(next, client, request, edgeResponseReceiver);
                }
            };

        }

        rootFilterChain = chain;

    }


    private void resolveAndDispatch(final Client client,
                                    final Request request,
                                    final EdgeResponseReceiver receiver) {

        final EdgeRequestPathHandler<?> edgeRequestPathHandler =
            resourceService.getResource(request.getHeader().getPath())
                           .getHandler(request.getHeader().getMethod());

        if (request.getPayload() == null) {
            edgeRequestPathHandler.handle(client, request, receiver);
        } else if (edgeRequestPathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
            edgeRequestPathHandler.handle(client, request, receiver);
        } else {
            throw new InvalidParameterException("Method " + request.getHeader().getPath() + " " +
                    "at path " + request.getHeader().getPath() +
                    "does not handle payload (" + request.getPayload() + ") " +
                    "of type " + request.getPayload().getClass());
        }

    }

    /**
     *
     * Essentially, this checks for two conditions.  First, it ensures that only
     * a single response is sent to the client.  In the event the request does
     * not generate a response, a null response is generated with an instance of
     * {@link ResponseCode#OK}.
     *
     * This uses an instance of {@link AtomicBoolean} to ensure that the response
     * is generated only once.
     *
     */
    private class DelegatingCheckedReceiver implements EdgeResponseReceiver, AutoCloseable {

        private final Request request;

        private final EdgeResponseReceiver delegate;

        private final AtomicBoolean received = new AtomicBoolean();

        public DelegatingCheckedReceiver(final Client client,
                                         final Request request,
                                         final EdgeResponseReceiver delegate) {
            this.request = request;
            this.delegate = delegate;
        }

        @Override
        public void receive(int code, Object payload) {
            if (received.compareAndSet(false, true)) {
                delegate.receive(code, payload);
            } else {
                LOG.error("Attempted to dispatch duplicate responses for request {}", request);
            }
        }

        @Override
        public void close()  {
            if (received.compareAndSet(false, true)) {

                final String msg = "EdgeRequestDispatcher failed to generate response.";

                final SimpleExceptionResponsePayload simpleExceptionResponsePayload;
                simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
                simpleExceptionResponsePayload.setMessage(msg);

                delegate.receive(ResponseCode.INTERNAL_ERROR_FATAL.getCode(), simpleExceptionResponsePayload);

            }
        }

    }

}
