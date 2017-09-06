package com.namazustudios.socialengine.rt.http;

import com.google.common.net.MediaType;
import com.namazustudios.socialengine.rt.exception.*;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;
import com.namazustudios.socialengine.rt.util.LazyValue;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.ANY_TYPE;
import static com.namazustudios.socialengine.rt.http.Accept.parseHeader;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

/**
 * Builds an {@link HttpManifestMetadata} as a composite of an {@link HttpRequest} and a {@link HttpManifest}.  All
 * computation is deferred as each of the methods are called.
 */
public class CompositeHttpManifestMetadata implements HttpManifestMetadata {

    private final LazyValue<HttpRequest> httpRequest;

    private final LazyValue<HttpManifest> httpManifest;

    private final LazyValue<HttpModule> httpModule = new LazyValue<>(this::resolveModule);

    public CompositeHttpManifestMetadata(final Supplier<HttpRequest> httpRequestSupplier,
                                         final Supplier<HttpManifest> httpManifestSupplier) {
        this.httpRequest = new LazyValue<>(httpRequestSupplier);
        this.httpManifest = new LazyValue<>(httpManifestSupplier);
    }

    private List<Accept> getAcceptableContentTypes() {

        final List<Accept> accepts = httpRequest.get()
            .getHeader()
            .getHeaders()
            .getOrDefault(ACCEPT, asList(ANY_TYPE))
            .stream()
            .flatMap(header -> parseHeader(header.toString()).stream())
            .collect(Collectors.toList());

        sort(accepts);

        return unmodifiableList(accepts);

    }

    private boolean isAcceptable(final List<Accept> accepts, final HttpOperation operation) {

        final Predicate<HttpContent> acceptable = content -> {

            final MediaType producedMediaType;

            try {
                producedMediaType = MediaType.parse(content.getType());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(ex);
            }

            return accepts.stream().anyMatch(accepted -> producedMediaType.is(accepted.getMediaType()));

        };

        return operation.getProducesContentByType()
            .values()
            .stream()
            .anyMatch(acceptable);

    }

    private boolean isConsumable(final MediaType contentType, final HttpOperation operation) {

        final Predicate<HttpContent> consumable = content -> {

            final MediaType consumedMediaType;

            try {
                consumedMediaType = MediaType.parse(content.getType());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(ex);
            }

            return contentType.is(consumedMediaType);

        };

        return operation.getConsumesContentByType()
            .values()
            .stream()
            .anyMatch(consumable);

    }

    private MediaType getContentType() {

        final String contentTypeHeader = httpRequest
            .get()
            .getHeader()
            .getHeaderOrDefault(CONTENT_TYPE, ANY_TYPE)
            .toString();

        try {
            return MediaType.parse(contentTypeHeader);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }

    }

    private HttpModule resolveModule() {
        return null;
    }

    @Override
    public HttpManifest getManifest() {
        return httpManifest.get();
    }

    @Override
    public HttpModule getModule() {
        return httpModule.get();
    }

    @Override
    public boolean hasSinglePreferredOperation() {

        final HttpRequest req = httpRequest.get();

        final List<HttpOperation> httpOperationList = httpManifest.get()
                .getModulesByName()
                .values()
                .stream()
                .flatMap(module -> module.getOperationsByName().values().stream())
                .collect(Collectors.toList());

        final MediaType contentType = getContentType();
        final List<Accept> acceptableContentTypes = getAcceptableContentTypes();

        httpOperationList.removeIf(op -> !op.getPath().matches(req.getHeader().getParsedPath()));
        httpOperationList.removeIf(op -> !op.getVerb().equals(req.getVerb()));
        httpOperationList.removeIf(op -> !isAcceptable(acceptableContentTypes, op));
        httpOperationList.removeIf(op -> !isConsumable(contentType, op));

        return httpOperationList.size() == 1;

    }

    @Override
    public HttpOperation getPreferredOperation() {

        final List<HttpOperation> httpOperationList = getAvailableOperations();

        if (httpOperationList.isEmpty()) {
            throw new UnsupportedMediaTypeException("unsupported content type " + getContentType());
        } else if (httpOperationList.size() > 1) {
            throw new BadRequestException("multiple operations match request");
        } else {
            return httpOperationList.get(0);
        }

    }

    @Override
    public List<HttpOperation> getAvailableOperations() {

        final HttpRequest req = httpRequest.get();

        final List<HttpOperation> httpOperationList = httpManifest.get()
                .getModulesByName()
                .values()
                .stream()
                .flatMap(module -> module.getOperationsByName().values().stream())
                .collect(Collectors.toList());

        httpOperationList.removeIf(op -> !op.getPath().matches(req.getHeader().getParsedPath()));

        if (httpOperationList.isEmpty()) {
            throw new OperationNotFoundException("no operation for path " + req.getHeader().getPath());
        }

        httpOperationList.removeIf(op -> !op.getVerb().equals(req.getVerb()));

        if (httpOperationList.isEmpty()) {
            throw new VerbNotSupportedException("no operation for verb " + req.getVerb());
        }

        final List<Accept> acceptableContentTypes = getAcceptableContentTypes();
        httpOperationList.removeIf(op -> !isAcceptable(acceptableContentTypes, op));

        if (httpOperationList.isEmpty()) {
            throw new UnacceptableContentException("unacceptable content");
        }

        final MediaType contentType = getContentType();
        httpOperationList.removeIf(op -> !isConsumable(contentType, op));

        return unmodifiableList(httpOperationList);

    }

}
