package dev.getelements.elements.servlet;

import dev.getelements.elements.servlet.security.HttpPathUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import static dev.getelements.elements.sdk.model.Constants.HTTP_PATH_PREFIX;
import static java.lang.String.format;

public class HttpContextRoot {

    private String httpPathPrefix;

    public String normalize(final String suffix) {
        final var full = format("%s/%s", getHttpPathPrefix(), suffix);
        return HttpPathUtils.normalize(full);
    }

    public String formatNormalized(final String format, final String ... args) {
        final var formatted = format(format, args);
        return normalize(formatted);
    }

    public String getHttpPathPrefix() {
        return httpPathPrefix;
    }

    @Inject
    public void setHttpPathPrefix(@Named(HTTP_PATH_PREFIX) String httpPathPrefix) {
        this.httpPathPrefix = httpPathPrefix;
    }

}
