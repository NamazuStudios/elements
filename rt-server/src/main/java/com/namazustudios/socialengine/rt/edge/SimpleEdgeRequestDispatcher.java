package com.namazustudios.socialengine.rt.edge;

import com.namazustudios.socialengine.exception.BaseException;
import com.namazustudios.socialengine.exception.InvalidParameterException;
import com.namazustudios.socialengine.rt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * The simple implementation of the {@link EdgeRequestDispatcher} interface.
 *
 * Created by patricktwohig on 7/27/15.
 */
public class SimpleEdgeRequestDispatcher implements EdgeRequestDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleEdgeRequestDispatcher.class);

    @Inject
    private ResourceService<EdgeResource> resourceService;

    @Inject
    private ExceptionMapper.Resolver exceptionMapperResolver;

    private EdgeFilter.Chain rootFilterChain;

    @Override
    public void dispatch(final EdgeClient edgeClient,
                         final Request request,
                         final ResponseReceiver responseReceiver) {
        try (final DelegatingCheckedResponseReceiver receiver = new DelegatingCheckedResponseReceiver(request, responseReceiver)) {
            executeRootFilterChain(edgeClient, request, receiver);
        } catch (Exception ex) {
            LOG.error("Caught exception processing request {}.", request, ex);
        }
    }

    private void executeRootFilterChain(final EdgeClient edgeClient,
                                        final Request request,
                                        final ResponseReceiver responseReceiver) {
        try {
            rootFilterChain.next(edgeClient, request, responseReceiver);
        } catch (Exception ex) {
            mapException(ex, edgeClient, request, responseReceiver);
        }
    }

    private <T extends Exception> void mapException(final T ex,
                                                    final EdgeClient edgeClient,
                                                    final Request request,
                                                    final ResponseReceiver responseReceiver) {
        LOG.info("Mapping exception for request {} and edgeClient {}", request, edgeClient, ex);
        final ExceptionMapper<T> exceptionMapper = exceptionMapperResolver.getExceptionMapper(ex);
        exceptionMapper.map(ex, request, responseReceiver);
    }

    @Inject
    public void buildRootFilterChain(final List<EdgeFilter> edgeFilterList) {

        Collections.reverse(edgeFilterList);

        EdgeFilter.Chain chain = new EdgeFilter.Chain() {
            @Override
            public void next(final EdgeClient client,
                             final Request request,
                             final ResponseReceiver receiver) {
                resolveAndDispatch(client, request, receiver);
            }
        };

        for (final EdgeFilter filter : edgeFilterList) {

            final EdgeFilter.Chain next = chain;

            chain = new EdgeFilter.Chain() {
                @Override
                public void next(final EdgeClient edgeClient,
                                 final Request request,
                                 final ResponseReceiver responseReceiver) {
                    filter.filter(next, edgeClient, request, responseReceiver);
                }
            };

        }

        rootFilterChain = chain;

    }

    private void resolveAndDispatch(final EdgeClient edgeClient,
                                    final Request request,
                                    final ResponseReceiver receiver) {

        final Path path = new Path(request.getHeader().getPath());

        final EdgeRequestPathHandler edgeRequestPathHandler =
            resourceService.getResource(path)
                           .getHandler(request.getHeader().getMethod());

        if (request.getPayload() == null) {
            edgeRequestPathHandler.handle(edgeClient, request, receiver);
        } else if (edgeRequestPathHandler.getClass().isAssignableFrom(request.getPayload().getClass())) {
            edgeRequestPathHandler.handle(edgeClient, request, receiver);
        } else {
            throw new InvalidParameterException("Method " + request.getHeader().getPath() + " " +
                    "at path " + request.getHeader().getPath() +
                    "does not handle payload (" + request.getPayload() + ") " +
                    "of type " + request.getPayload().getClass());
        }

    }

}
