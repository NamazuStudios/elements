package com.namazustudios.socialengine.rt.internal;

import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * The simple implementation of the {@link InternalRequestDispatcher}.
 *
 * Created by patricktwohig on 8/23/15.
 */
public class SimpleInternalRequestDispatcher implements InternalRequestDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleInternalRequestDispatcher.class);

    @Inject
    private ExceptionMapper.Resolver exceptionMapperResolver;

    @Inject
    private ResourceService<InternalResource> resourceService;

    @Override
    public void dispatch(final Request request, final ResponseReceiver responseReceiver) {
        try (final DelegatingCheckedResponseReceiver receiver = new DelegatingCheckedResponseReceiver(request, responseReceiver)) {
            doDispatch(request, responseReceiver);
        }  catch (Exception ex) {
            LOG.error("Caught exception processing request {}.", request, ex);
        }
    }

    private void doDispatch(final Request request,
                            final ResponseReceiver receiver) {
        try {
            resolveAndDispatch(request, receiver);
        } catch (Exception ex) {
            mapException(ex, request, receiver);
        }
    }

    private void resolveAndDispatch(final Request request,
                                    final ResponseReceiver receiver) {

        final Path path = new Path(request.getHeader().getPath());

        final InternalRequestPathHandler edgeRequestPathHandler =
                resourceService.getResource(path)
                               .getHandler(request.getHeader().getMethod());

        if (request.getPayload() == null) {
            edgeRequestPathHandler.handle(request, receiver);
        } else if (edgeRequestPathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
            edgeRequestPathHandler.handle(request, receiver);
        } else {
            throw new InvalidParameterException("Method " + request.getHeader().getPath() + " " +
                    "at path " + request.getHeader().getPath() +
                    "does not handle payload (" + request.getPayload() + ") " +
                    "of type " + request.getPayload().getClass());
        }

    }


    private <T extends Exception> void mapException(final T ex,
                                                    final Request request,
                                                    final ResponseReceiver responseReceiver) {

        try {

            LOG.info("Mapping exception for request {} and edgeClient {}", request, ex);

            final ExceptionMapper<T> exceptionMapper = exceptionMapperResolver.getExceptionMapper(ex);

            if (exceptionMapper == null) {
                mapUnhandled(ex, request, responseReceiver);
            } else {
                exceptionMapper.map(ex, request, responseReceiver);
            }

        } catch (Exception _ex) {
            LOG.error("Caught exception attempting to forumulate exception response from request {} for edgeClient {} ", request, _ex);
            mapUnhandled(_ex, request, responseReceiver);
        }

    }

    private <T extends Exception> void mapUnhandled(final T ex,
                                                    final Request request,
                                                    final ResponseReceiver responseReceiver) {

        final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
        simpleExceptionResponsePayload.setMessage(ex.getMessage());

        ResponseCode code;

        try {
            throw ex;
        } catch (BaseException bex) {
            code = ExceptionMapper.RESPONSE_STATUS_MAP.get(bex.getCode());
            code = code == null ? ResponseCode.INTERNAL_ERROR_FATAL : code;
            LOG.warn("Caught exception handling request {} to edgeClient {}.", request, bex);
        } catch (Exception e) {
            code = ResponseCode.INTERNAL_ERROR_FATAL;
            LOG.error("Caught exception handling request {} to edgeClient {}.", request, e);
        }

        final SimpleResponse simpleResponse = SimpleResponse.builder()
                .from(request)
                .code(code)
                .payload(simpleExceptionResponsePayload)
            .build();

        responseReceiver.receive(simpleResponse);

    }

}
