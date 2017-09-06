package com.namazustudios.socialengine.rt.http;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.namazustudios.socialengine.rt.manifest.http.HttpContent;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;
import com.namazustudios.socialengine.rt.manifest.http.HttpModule;
import com.namazustudios.socialengine.rt.manifest.http.HttpOperation;

import java.util.List;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.rt.http.AcceptableContentType.parseHeader;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;

public class CompositeHttpManifestMetadata implements HttpManifestMetadata {

    private final HttpRequest httpRequest;

    private final HttpManifest httpManifest;

    private final HttpModule httpModule;

    private final List<AcceptableContentType> acceptableContentTypes;

    private final HttpOperation preferredOperation;

    private final List<HttpOperation> availableOperations;

    public CompositeHttpManifestMetadata(HttpRequest httpRequest, HttpManifest httpManifest) {
        this.httpRequest = httpRequest;
        this.httpManifest = httpManifest;
        this.httpModule = resolveModule();
        this.acceptableContentTypes = buildAcceptableTypes();
        this.preferredOperation = resolvePreferredOperation();
        this.availableOperations = resolveAvailbleOperations();
    }

    private List<AcceptableContentType> buildAcceptableTypes() {

        final List<AcceptableContentType> acceptableContentTypes = httpRequest
            .getHeader()
            .getHeaders()
            .getOrDefault(HttpHeaders.ACCEPT, asList(MediaType.ANY_TYPE.toString()))
            .stream()
            .flatMap(header -> parseHeader(header.toString()).stream())
            .collect(Collectors.toList());

        sort(acceptableContentTypes);

        return unmodifiableList(acceptableContentTypes);

    }

    private HttpModule resolveModule() {
        return null;
    }

    private HttpOperation resolvePreferredOperation() {
        return null;
    }

    private List<HttpOperation> resolveAvailbleOperations() {
        return null;
    }

    @Override
    public HttpManifest getManifest() {
        return httpManifest;
    }

    @Override
    public HttpModule getModule() {
        return httpModule;
    }

    @Override
    public boolean hasSinglePreferredOperation() {
        return preferredOperation != null;
    }

    @Override
    public HttpOperation getPreferredOperation() {
        return preferredOperation;
    }

    @Override
    public List<HttpOperation> getAvailableOperations() {
        return availableOperations;
    }

    @Override
    public HttpContent getContentFor(final HttpOperation operation) {
        return null;
    }

}
