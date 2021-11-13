package com.namazustudios.socialengine.rt.http;

import com.google.common.net.MediaType;
import com.namazustudios.socialengine.rt.exception.*;
import com.namazustudios.socialengine.rt.manifest.http.*;
import com.namazustudios.socialengine.rt.util.LazyValue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.common.net.HttpHeaders.ACCEPT;
import static com.google.common.net.HttpHeaders.CONTENT_TYPE;
import static com.google.common.net.MediaType.ANY_TYPE;
import static com.google.common.net.MediaType.parse;
import static com.namazustudios.socialengine.rt.http.Accept.parseHeader;
import static com.namazustudios.socialengine.rt.manifest.http.HttpVerb.*;
import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static java.util.stream.Collectors.joining;

/**
 * Builds an {@link HttpManifestMetadata} as a composite of an {@link HttpRequest} and a {@link HttpManifest}.  All
 * computation is deferred as each of the methods are called.
 */
public class CompositeHttpManifestMetadata implements HttpManifestMetadata {

    private final HttpRequest httpRequest;

    private final HttpManifest httpManifest;

    private final LazyValue<HttpModule> httpModule = new LazyValue<>(this::resolveModule);

    public CompositeHttpManifestMetadata(final HttpRequest httpRequest, final HttpManifest httpManifest) {
        this.httpRequest = httpRequest;
        this.httpManifest = httpManifest;
    }

    private List<Accept> getAcceptableContentTypes() {

        final List<Accept> accepts = httpRequest
            .getHeader()
            .getHeaders(ACCEPT).orElseGet(() -> singletonList(ANY_TYPE))
            .stream()
            .flatMap(header -> parseHeader(header.toString()).stream())
            .collect(Collectors.toList());

        sort(accepts);

        return unmodifiableList(accepts);

    }

    private boolean isAcceptable(final List<Accept> accepts, final HttpOperation operation) {
        return operation.getProducesContentByType()
            .values()
            .stream()
            .anyMatch(c -> isAcceptable(accepts, c));
    }

    private boolean isAcceptable(final List<Accept> accepts, final HttpContent httpContent) {

        final MediaType producedMediaType;

        try {
            producedMediaType = parse(httpContent.getType());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex);
        }

        return accepts.stream().anyMatch(accepted -> producedMediaType.is(accepted.getMediaType()));

    }

    private boolean isConsumable(final MediaType contentType, final HttpOperation operation) {

        final Predicate<HttpContent> consumable = content -> {

            final MediaType consumedMediaType;

            try {
                consumedMediaType = parse(content.getType());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException(ex);
            }

            return consumedMediaType.is(contentType);

        };

        return operation.getConsumesContentByType()
            .values()
            .stream()
            .anyMatch(consumable);

    }

    private MediaType getContentType(final MediaType defaultMediaType) {
        return httpRequest
            .getHeader()
            .getHeader(CONTENT_TYPE)
            .map(Object::toString)
            .map(header -> {
                try {
                    return parse(header);
                } catch (IllegalArgumentException ex) {
                    throw new BadRequestException(ex);
                }
            }).orElse(defaultMediaType);
    }

    private HttpModule resolveModule() {

        final HttpOperation operation = getPreferredOperation();


        return httpManifest.getModulesByName()
            .values()
            .stream()
            .filter(module -> module.getOperationsByName().containsValue(operation))
            .findFirst().orElseThrow(() -> new OperationNotFoundException());

    }

    @Override
    public HttpManifest getManifest() {
        return httpManifest;
    }

    @Override
    public HttpModule getModule() {
        return httpModule.get();
    }

    @Override
    public boolean hasSinglePreferredOperation() {

        final HttpRequest req = httpRequest;

        final List<HttpOperation> httpOperationList = httpManifest
                .getModulesByName()
                .values()
                .stream()
                .flatMap(module -> module.getOperationsByName().values().stream())
                .collect(Collectors.toList());

        final var acceptableContentTypes = getAcceptableContentTypes();

        httpOperationList.removeIf(op -> !op.getPath().matches(req.getHeader().getParsedPath()));
        httpOperationList.removeIf(op -> !op.getVerb().equals(req.getVerb()));
        httpOperationList.removeIf(op -> !isAcceptable(acceptableContentTypes, op));

        final var contentType = getContentType(ANY_TYPE);
        httpOperationList.removeIf(op -> !isConsumable(contentType, op));

        return httpOperationList.size() == 1;

    }

    @Override
    public HttpOperation getPreferredOperation() {

        final List<HttpOperation> httpOperationList = getAvailableOperations();

        if (httpOperationList.isEmpty()) {
            throw new UnsupportedMediaTypeException("unsupported content type " + getContentType(null));
        } else if (httpOperationList.size() > 1) {
            throw new BadRequestException("multiple operations match request");
        } else {
            return httpOperationList.get(0);
        }

    }

    @Override
    public List<HttpOperation> getAvailableOperations() {

        final HttpRequest req = httpRequest;

        final List<HttpOperation> httpOperationList = httpManifest
                .getModulesByName()
                .values()
                .stream()
                .flatMap(module -> module.getOperationsByName().values().stream())
                .collect(Collectors.toList());

        httpOperationList.removeIf(op -> !op.getPath().matches(req.getHeader().getParsedPath()));

        if (httpOperationList.isEmpty()) {
            throw new OperationNotFoundException("no operation for path " + req.getHeader().getPath());
        }

        // A few edge cases for the OPTIONS and the HEAD

        final HttpVerb verb = req.getVerb();

        switch (verb) {

            case OPTIONS:

                // If we have an explicit options request, then we filter out everything that's not an options request.
                // Otherwise, we advertise all of the available operations that may be handled.

                if (httpOperationList.stream().anyMatch(op -> op.getVerb().equals(OPTIONS))) {
                    httpOperationList.removeIf(op -> !op.getVerb().equals(OPTIONS));
                }

                break;

            case HEAD:

                // If we have an explicit HEAD request, then we filter out everything that's not explicitly defined
                // by the

                if (httpOperationList.stream().anyMatch(op -> op.getVerb().equals(HEAD))) {
                    httpOperationList.stream().anyMatch(op -> !op.getVerb().equals(HEAD));
                } else {
                    httpOperationList.removeIf(op -> !op.getVerb().equals(GET));
                }

                break;

            default:
                // We search directly for the requested HTTP verb
                httpOperationList.removeIf(op -> !op.getVerb().equals(req.getVerb()));
                break;
        }

        if (httpOperationList.isEmpty()) {
            throw new VerbNotSupportedException("no operation for verb " + req.getVerb());
        }

        final List<Accept> acceptableContentTypes = getAcceptableContentTypes();
        httpOperationList.removeIf(op -> !isAcceptable(acceptableContentTypes, op));

        if (httpOperationList.isEmpty()) {
            throw new UnacceptableContentException("unacceptable content");
        }



        if ((POST.equals(verb) || PUT.equals(verb))) {
            final var contentType = getContentType(ANY_TYPE);
            httpOperationList.removeIf(op -> !isConsumable(contentType, op));
        }

        return unmodifiableList(httpOperationList);

    }

    @Override
    public HttpContent getPreferredRequestContent() {

        final var contentType = getContentType(ANY_TYPE);
        final var preferredOperation = getPreferredOperation();

        final var preferredContentList = preferredOperation
            .getConsumesContentByType()
            .values()
            .stream()
            .filter(c -> parse(c.getType()).is(contentType))
            .collect(Collectors.toList());

        if (preferredContentList.size() > 1) {
            preferredContentList.removeIf(c -> !c.isDefaultContent());
        }

        if (preferredContentList.isEmpty()) {
            throw new UnsupportedMediaTypeException("unsupported content type " + getContentType(null));
        } else if (preferredContentList.size() > 1) {
            final var matching = "[" + join(",", preferredOperation.getConsumesContentByType().keySet()) + "]";
            throw new InternalException(contentType + " matches multiple request content types" + matching);
        }

        return preferredContentList.get(0);

    }

    @Override
    public HttpContent getPreferredResponseContent() {

        final List<Accept> accepts = getAcceptableContentTypes();
        final HttpOperation preferredOperation = getPreferredOperation();

        final List<HttpContent> preferredContentList = preferredOperation
                .getProducesContentByType()
                .values()
                .stream()
                .filter(c -> isAcceptable(accepts, c))
                .collect(Collectors.toList());

        if (preferredContentList.size() > 1) {
            preferredContentList.removeIf(c -> !c.isDefaultContent());
        }

        if (preferredContentList.size() != 1) {

            final String acceptableTypes = "[" + httpRequest
                    .getHeader()
                    .getHeaders(ACCEPT).orElseGet(() -> asList(ANY_TYPE))
                    .stream()
                    .map(o -> o.toString())
                    .collect(joining(",")) + "]";

            final String matching = "[" + preferredOperation
                    .getConsumesContentByType()
                    .keySet()
                    .stream()
                    .collect(joining(",")) + "]";

            throw new InternalException(acceptableTypes + " matches multiple response types " + matching);

        }

        return preferredContentList.get(0);

    }

}
