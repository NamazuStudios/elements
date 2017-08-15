package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A singleton instance of {@link ExceptionMapper} which defines the default
 * behavior for exception mapping when no other exception mapper can be resolved.
 *
 * The behavior encapsulated here should be a reference for the standard exception mapping
 * behavior and should be used when an {@link ExceptionMapper.Resolver} can't provide
 * an {@link ExceptionMapper} for the desired type.
 *
 * Created by patricktwohig on 9/30/15.
 */
public class DefaultExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    private static final DefaultExceptionMapper INSTANCE = new DefaultExceptionMapper();

    private DefaultExceptionMapper() {}

    @Override
    public void map(final Throwable throwable,
                    final Request request,
                    final ResponseReceiver responseReceiver) {

        final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
        simpleExceptionResponsePayload.setMessage(throwable.getMessage());

        ResponseCode code;

        try {
            throw throwable;
        } catch (BaseException bex) {
            code = bex.getResponseCode();
            code = code == null ? ResponseCode.INTERNAL_ERROR_FATAL : code;
            LOG.warn("Caught exception handling request {}.", request, bex);
        } catch (Throwable th) {
            code = ResponseCode.INTERNAL_ERROR_FATAL;
            LOG.error("Caught exception handling request {}.", request, th);
        }

        final SimpleResponse simpleResponse = SimpleResponse.builder()
                .from(request)
                .code(code)
                .payload(simpleExceptionResponsePayload)
            .build();

        try {
            responseReceiver.receive(simpleResponse);
        } catch (Exception ex) {
            LOG.error("Caught exception mapping exception to response.", ex);
        }

    }

    public static DefaultExceptionMapper getInstance() {
        return INSTANCE;
    }

}
