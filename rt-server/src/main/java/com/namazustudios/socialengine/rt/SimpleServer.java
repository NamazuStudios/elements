package com.namazustudios.socialengine.rt;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.ErrorCode;
import com.namazustudios.socialengine.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The siple implementation of the {@link Server} inteface.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class SimpleServer implements Server {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleServer.class);

    private static final Map<ErrorCode, ResponseHeader.Code> RESPONSE_STATUS_MAP = Maps.immutableEnumMap(
        new ImmutableMap.Builder<ErrorCode, ResponseHeader.Code>()
            .put(ErrorCode.DUPLICATE, ResponseHeader.Code.BAD_REQUEST_FATAL)
            .put(ErrorCode.FORBIDDEN, ResponseHeader.Code.FAILED_AUTH_FATAL)
            .put(ErrorCode.INVALID_DATA, ResponseHeader.Code.BAD_REQUEST_FATAL)
            .put(ErrorCode.NOT_FOUND, ResponseHeader.Code.PATH_NOT_FOUND)
            .put(ErrorCode.OVERLOAD, ResponseHeader.Code.TOO_BUSY_FATAL)
            .put(ErrorCode.INVALID_PARAMETER, ResponseHeader.Code.BAD_REQUEST_FATAL)
            .put(ErrorCode.UNKNOWN, ResponseHeader.Code.INTERNAL_ERROR_FATAL)
        .build());

    @Inject
    private PathHandlerService pathHandlerService;

    @Inject
    private ConnectedClientService connectedClientService;

    @Inject
    private ExceptionMapper.Resolver exceptionMapperResolver;

    private Filter.Chain rootFilterChain;

    @Override
    public void handleRequest(final Client client, final Request request) {
        try (final DelegatingCheckedReceiver receiver = new DelegatingCheckedReceiver(client, request)) {
            executeRootFilterChain(client, request, receiver);
        } catch (Exception ex) {
            LOG.error("Caught exception processing request {}.", request, ex);
        }
    }

    private void executeRootFilterChain(final Client client,
                                        final Request<?> request,
                                        final Receiver<ResponseHeader, Object> receiver) {
        try {
            rootFilterChain.next(client, request, receiver);
        } catch (Exception ex) {
            handleExceptions(ex, client, request, receiver);
        }
    }

    private void resolveAndDispatch(final Client client,
                                    final Request<?> request,
                                    final Receiver<ResponseHeader, Object> receiver) {

        final PathHandler<Object> pathHandler = pathHandlerService.getPathHandler(request.getHeader());

        // The generics get a little screwy, but the interface demands that the
        // path handler return an instance compatible with the given type, so we just
        // force the cast anyhow.

        final Request<Object> objectRequest = (Request<Object>) request;

        if (request.getPayload() == null) {
            pathHandler.handle(null, receiver);
        } else if (pathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
            pathHandler.handle(objectRequest, receiver);
        } else {
            throw new InternalException("Method " + request.getHeader().getPath() + " " +
                                        "at path " + request.getHeader().getPath() +
                                        "does not handle payload (" + request.getPayload() + ") " +
                                        "of type " + request.getPayload().getClass());
        }

    }


    private <T extends Exception> void handleExceptions(final T ex,
                                                        final Client client,
                                                        final Request<?> request,
                                                        final Receiver<ResponseHeader, Object> receiver) {

        Response<?> response;

        try {

            LOG.info("Mapping exception for request {} and client {}", request, client, ex);

            final ExceptionMapper<T> exceptionMapper = (ExceptionMapper<T>)exceptionMapperResolver.getExceptionMapper(ex);

            if (exceptionMapper == null) {
                response = generateExceptionResponse(ex, client, request);
            } else {
                response = exceptionMapper.map(ex);
            }

        } catch (Exception _ex) {
            LOG.error("Caught exception attempting to forumulate exception response from request {} for client {} ", request, client, _ex);
            response = generateExceptionResponse(ex, client, request);
        }

        receiver.receive(response.getResponseHeader(), response.getPayload());

    }

    private  Response<?> generateExceptionResponse(final Exception ex,
                                                   final Client client,
                                                   final Request<?> request) {

        ResponseHeader.Code code;

        try {
            throw ex;
        } catch (BaseException bex) {
            code = RESPONSE_STATUS_MAP.get(bex.getCode());
            code = code == null ? ResponseHeader.Code.INTERNAL_ERROR_FATAL : code;
            LOG.warn("Caught exception handling request {} to client {}.", request, client, bex);
        } catch (Exception e) {
            code = ResponseHeader.Code.INTERNAL_ERROR_FATAL;
            LOG.error("Caught exception handling request {} to client {}.", request, client, e);
        }

        return generateResponseForError(code, ex.getMessage(), request);

    }

    private SimpleResponse<SimpleExceptionResponsePayload> generateResponseForError(final ResponseHeader.Code code,
                                                                                    final String message,
                                                                                    final Request<?> request) {

        final SimpleResponseHeader simpleResponseHeader = new SimpleResponseHeader();
        simpleResponseHeader.setCode(code.getCode());
        simpleResponseHeader.setSequence(request.getHeader().getSequence());

        final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
        simpleExceptionResponsePayload.setMessage(message);

        final SimpleResponse<SimpleExceptionResponsePayload> errorResponseSimpleResponse = new SimpleResponse<>();
        errorResponseSimpleResponse.setResponseHeader(simpleResponseHeader);
        errorResponseSimpleResponse.setPayload(simpleExceptionResponsePayload);

        return errorResponseSimpleResponse;

    }

    @Inject
    void buildRootFilterChain(final List<Filter> filterList) {

        Collections.reverse(filterList);

        Filter.Chain chain = new Filter.Chain() {
            @Override
            public void next(Client client, Request<?> request, Receiver<ResponseHeader, Object> receiver) {
                resolveAndDispatch(client, request, receiver);
            }
        };

        for (final Filter filter : filterList) {

            final Filter.Chain next = chain;

            chain = new Filter.Chain() {
                @Override
                public void next(Client client, Request<?> request, Receiver<ResponseHeader, Object> receiver) {
                    filter.filter(next, client, request, receiver);
                }
            };

        }

        rootFilterChain = chain;

    }

    /**
     *
     * Essentially, this checks for two conditions.  First, it ensures that only
     * a single response is sent to the client.  In the event the request does
     * not generate a response, a null response is generated with an instance of
     * {@link ResponseHeader.Code#OK}.
     *
     * This uses an instance of {@link AtomicBoolean} to ensure that the response
     * is generated only once.
     *
     */
    private class DelegatingCheckedReceiver implements Receiver<ResponseHeader, Object>, AutoCloseable {

        final Client client;

        final Request<?> request;

        final Receiver<ResponseHeader, Object> delegate;

        final AtomicBoolean received = new AtomicBoolean();

        public DelegatingCheckedReceiver(final Client client, final Request<?> request) {
            this.client = client;
            this.request = request;
            this.delegate = connectedClientService.getResponseReceiver(client, Object.class);;
        }

        @Override
        public void receive(ResponseHeader header, Object payload) {
            if (received.compareAndSet(false, true)) {
                final Receiver<ResponseHeader, Object> receiver;
                receiver = connectedClientService.getResponseReceiver(client, Object.class);
                receiver.receive(header, payload);
            } else {
                LOG.error("Attempted to dispatch duplicate responses for request {}", request);
            }
        }

        @Override
        public void close()  {
            if (received.compareAndSet(false, true)) {

                final Response<?> response = generateResponseForError(ResponseHeader.Code.INTERNAL_ERROR_FATAL,
                                                                      "Server failed to generate response.",
                                                                      request);

                delegate.receive(response.getResponseHeader(), response.getPayload());

            }
        }

    }

}
