package dev.getelements.elements.servlet;

import dev.getelements.elements.servlet.security.HttpPathUtils;

import javax.inject.Inject;
import javax.inject.Named;

import static dev.getelements.elements.Constants.HTTP_PATH_PREFIX;

public class HttpContextRoot {

    private String httpPathPrefix;

    public String normalize(final String suffix) {
        final var full = String.format("%s/%s", getHttpPathPrefix(), suffix);
        return HttpPathUtils.normalize(full);
    }

    public String formatNormalized(final String format, final String ... args) {
        final var formatted = formatNormalized(format, args);
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
