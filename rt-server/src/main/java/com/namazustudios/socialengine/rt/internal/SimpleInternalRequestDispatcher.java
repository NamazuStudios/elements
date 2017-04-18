package com.namazustudios.socialengine.rt.internal;

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

    private ExceptionMapper.Resolver exceptionMapperResolver;

    private Container<InternalResource> internalResourceContainer;

    @Override
    public void dispatch(final Request request, final ResponseReceiver responseReceiver) {
        try (final DelegatingCheckedResponseReceiver receiver = new DelegatingCheckedResponseReceiver(request, responseReceiver)) {
            doDispatch(request, receiver);
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

        getInternalResourceContainer().performV(path, resource -> {

            final InternalRequestPathHandler internalRequestPathHandler;
            internalRequestPathHandler = resource.getHandler(request.getHeader().getMethod());

            if (request.getPayload() == null) {
                internalRequestPathHandler.handle(request, receiver);
            } else if (internalRequestPathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
                internalRequestPathHandler.handle(request, receiver);
            } else {
                throw new InvalidParameterException("Method " + request.getHeader().getPath() + " " +
                        "at path " + request.getHeader().getPath() +
                        "does not handle payload (" + request.getPayload() + ") " +
                        "of type " + request.getPayload().getClass());
            }

        });

    }

    private <T extends Exception> void mapException(final T ex,
                                                    final Request request,
                                                    final ResponseReceiver responseReceiver) {

        LOG.info("Mapping exception for request {} and edgeClient {}", request, ex);

        final ExceptionMapper<T> exceptionMapper;
        exceptionMapper = getExceptionMapperResolver().getExceptionMapper(ex);
        exceptionMapper.map(ex, request, responseReceiver);

    }

    public ExceptionMapper.Resolver getExceptionMapperResolver() {
        return exceptionMapperResolver;
    }

    @Inject
    public void setExceptionMapperResolver(ExceptionMapper.Resolver exceptionMapperResolver) {
        this.exceptionMapperResolver = exceptionMapperResolver;
    }

    public Container<InternalResource> getInternalResourceContainer() {
        return internalResourceContainer;
    }

    @Inject
    public void setInternalResourceContainer(Container<InternalResource> internalResourceContainer) {
        this.internalResourceContainer = internalResourceContainer;
    }

}
