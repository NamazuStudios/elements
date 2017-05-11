package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.exception.InternalException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Created by patricktwohig on 6/12/15.
 */
@PreMatching
public class ShortLinkForwardingFilter implements ContainerRequestFilter {

    private String apiRoot;

    private URI shortLinkBase;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        final URI uri = containerRequestContext.getUriInfo().getRequestUri();

        if (isShortLink(uri)) {
            final URI rewritten = rewrite(uri);
            containerRequestContext.setRequestUri(rewritten);
        }

    }

    private boolean isShortLink(final URI uri) {
        return Objects.equals(uri.getHost(), getShortLinkBase().getHost()) &&
                uri.getPath() != null &&
                uri.getPath().startsWith(getShortLinkBase().getPath());
    }

    private URI rewrite(final URI original)  {

        final String path = original.getPath().replaceFirst(shortLinkBase.getPath(), "");

        try {
            return new URI(
                original.getScheme(),
                original.getAuthority(),
                getApiRoot() + "/" + "short_link/redirection" + path,
                original.getQuery(),
                original.getFragment());
        } catch (URISyntaxException ex) {
            throw new InternalException(ex);
        }

    }

    private HttpServletRequest rewrite(final URI rewritten, final HttpServletRequest request) throws ServletException {

        return new HttpServletRequestWrapper(request) {

            @Override
            public String getRequestURI() {

                final StringBuilder sb = new StringBuilder(rewritten.getPath());

                if (rewritten.getQuery() != null) {
                    sb.append("?").append(rewritten.getQuery());
                }

                if (rewritten.getFragment() != null) {
                    sb.append("#").append(rewritten.getFragment());
                }

                return sb.toString();

            }

            @Override
            public StringBuffer getRequestURL() {
                return new StringBuffer(rewritten.toString());
            }

        };

    }

    public URI getShortLinkBase() {
        return shortLinkBase;
    }

    @Inject
    public void setShortLinkBase(@Named(Constants.SHORT_LINK_BASE) URI shortLinkBase) {
        this.shortLinkBase = shortLinkBase;
    }

    public String getApiRoot() {
        return apiRoot;
    }

    @Inject
    public void setApiRoot(@Named(Constants.API_OUTSIDE_URL) String apiRoot) {
        this.apiRoot = apiRoot;
    }

}
