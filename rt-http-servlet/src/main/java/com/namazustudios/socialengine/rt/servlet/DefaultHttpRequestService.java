package com.namazustudios.socialengine.rt.servlet;

import com.namazustudios.socialengine.rt.http.HttpRequest;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.function.Supplier;

public class DefaultHttpRequestService implements HttpRequestService {

    private Supplier<HttpManifest> httpManifestSupplier;

    @Override
    public HttpRequest getRequest(final HttpServletRequest req) {
        final HttpManifest httpManifest = getHttpManifestSupplier().get();
        return new ServletHttpRequest(req, httpManifest);
    }

    public Supplier<HttpManifest> getHttpManifestSupplier() {
        return httpManifestSupplier;
    }

    @Inject
    public void setHttpManifestSupplier(Supplier<HttpManifest> httpManifestSupplier) {
        this.httpManifestSupplier = httpManifestSupplier;
    }

}
