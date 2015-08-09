package com.namazustudios.socialengine.rt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The siple implementation of the {@link RequestDispatcher} interface.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class SimpleRequestDispatcher implements RequestDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleRequestDispatcher.class);

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
    private ResourceService resourceService;

    @Inject
    private ConnectedClientService connectedClientService;

    @Inject
    private ExceptionMapper.Resolver exceptionMapperResolver;

    private Filter.Chain rootFilterChain;

    @Override
    public void dispatch(final Client client, final Request request) {
        try (final DelegatingCheckedReceiver receiver = new DelegatingCheckedReceiver(client, request)) {
            executeRootFilterChain(client, request, receiver);
        } catch (Exception ex) {
            LOG.error("Caught exception processing request {}.", request, ex);
        }
    }

    private void executeRootFilterChain(final Client client,
                                        final Request request,
                                        final ConnectedClientService.ResponseReceiver responseReceiver) {
        try {
            rootFilterChain.next(client, request, responseReceiver);
        } catch (Exception ex) {
            mapException(ex, client, request, responseReceiver);
        }
    }

    private <T extends Exception> void mapException(final T ex,
                                                    final Client client,
                                                    final Request request,
                                                    final ConnectedClientService.ResponseReceiver responseReceiver) {

        try {

            LOG.info("Mapping exception for request {} and client {}", request, client, ex);

            final ExceptionMapper<T> exceptionMapper = exceptionMapperResolver.getExceptionMapper(ex);

            if (exceptionMapper == null) {
                mapUnhandled(ex, client, request, responseReceiver);
            } else {
                exceptionMapper.map(ex, client, request, responseReceiver);
            }

        } catch (Exception _ex) {
            LOG.error("Caught exception attempting to forumulate exception response from request {} for client {} ", request, client, _ex);
            mapUnhandled(_ex, client, request, responseReceiver);
        }

    }

    private <T extends Exception> void mapUnhandled(final T ex,
                                                    final Client client,
                                                    final Request request,
                                                    final ConnectedClientService.ResponseReceiver responseReceiver) {

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

        responseReceiver.receive(code.getCode(), simpleExceptionResponsePayload);

    }

    @Inject
    public void buildRootFilterChain(final List<Filter> filterList) {

        Collections.reverse(filterList);

        Filter.Chain chain = new Filter.Chain() {
            @Override
            public void next(final Client client,
                             final Request request,
                             final ConnectedClientService.ResponseReceiver receiver) {
                resolveAndDispatch(client, request, receiver);
            }
        };

        for (final Filter filter : filterList) {

            final Filter.Chain next = chain;

            chain = new Filter.Chain() {
                @Override
                public void next(final Client client,
                                 final Request request,
                                 final ConnectedClientService.ResponseReceiver responseReceiver) {
                    filter.filter(next, client, request, responseReceiver);
                }
            };

        }

        rootFilterChain = chain;

    }


    private void resolveAndDispatch(final Client client,
                                    final Request request,
                                    final ConnectedClientService.ResponseReceiver receiver) {

        final RequestPathHandler<?> requestPathHandler = resourceService.getPathHandler(request.getHeader());

        if (request.getPayload() == null) {
            requestPathHandler.handle(client, request, receiver);
        } else if (requestPathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
            requestPathHandler.handle(client, request, receiver);
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
    private class DelegatingCheckedReceiver implements ConnectedClientService.ResponseReceiver, AutoCloseable {

        private final Request request;

        private final ConnectedClientService.ResponseReceiver delegate;

        private final AtomicBoolean received = new AtomicBoolean();

        public DelegatingCheckedReceiver(final Client client,
                                         final Request request) {
            this.request = request;
            this.delegate = connectedClientService.getResponseReceiver(client, request);
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

                final String msg = "RequestDispatcher failed to generate response.";

                final SimpleExceptionResponsePayload simpleExceptionResponsePayload;
                simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
                simpleExceptionResponsePayload.setMessage(msg);

                delegate.receive(ResponseCode.INTERNAL_ERROR_FATAL.getCode(), simpleExceptionResponsePayload);

            }
        }

    }

}
