package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

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

    private static final Logger logger = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    private static final DefaultExceptionMapper INSTANCE = new DefaultExceptionMapper();

    private DefaultExceptionMapper() {}

    @Override
    public void map(final Throwable throwable, final Consumer<Response> responseReceiver) {

        final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
        logger.info("Mapping exception.", throwable);

        simpleExceptionResponsePayload.setMessage(throwable.getMessage());

        ResponseCode code;

        try {
            throw throwable;
        } catch (BaseException bex) {
            code = bex.getResponseCode();
            code = code == null ? ResponseCode.INTERNAL_ERROR_FATAL : code;
        } catch (Error error) {
            throw error;
        } catch (Throwable th) {
            code = ResponseCode.INTERNAL_ERROR_FATAL;
        }

        final SimpleResponse simpleResponse = SimpleResponse.builder()
                .code(code)
                .payload(simpleExceptionResponsePayload)
                .build();

        try {
            responseReceiver.accept(simpleResponse);
        } catch (Exception ex) {
            logger.error("Caught exception mapping exception to response.", ex);
        }

    }


    @Override
    public void map(final Throwable throwable,
                    final Request request,
                    final Consumer<Response> responseReceiver) {

        final SimpleExceptionResponsePayload simpleExceptionResponsePayload = new SimpleExceptionResponsePayload();
        simpleExceptionResponsePayload.setMessage(throwable.getMessage());

        ResponseCode code;

        try {
            throw throwable;
        } catch (BaseException bex) {
            code = bex.getResponseCode();
            code = code == null ? ResponseCode.INTERNAL_ERROR_FATAL : code;
            logger.warn("Caught exception handling request {}.", request, bex);
        } catch (Throwable th) {
            code = ResponseCode.INTERNAL_ERROR_FATAL;
            logger.error("Caught exception handling request {}.", request, th);
        }

        final SimpleResponse simpleResponse = SimpleResponse.builder()
            .from(request)
            .code(code)
            .payload(simpleExceptionResponsePayload)
        .build();

        try {
            responseReceiver.accept(simpleResponse);
        } catch (Exception ex) {
            logger.error("Caught exception mapping exception to response.", ex);
        }

    }

    public static DefaultExceptionMapper getInstance() {
        return INSTANCE;
    }

}
