package com.namazustudios.socialengine;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Created by patricktwohig on 6/12/15.
 */
public class ShortLinkForwardingFilter implements Filter {

    /**
     * The web API root.  Some instances may need to know this in order to properly redirect
     * or forward requests
     */
    public static final String API_ROOT = "com.namazustudios.socialengine.api.root";

    public static final String API_ROOT_DEFAULT = "/api";

    private String apiRoot;

    private URI shortLinkBase;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        apiRoot = filterConfig.getInitParameter(API_ROOT) == null ? API_ROOT_DEFAULT :
                  filterConfig.getInitParameter(API_ROOT);
        apiRoot = apiRoot.replace("/+$", "");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        final URI uri;

        try {
            uri = new URI(httpServletRequest.getRequestURL().toString());
        } catch (URISyntaxException ex) {
            throw new ServletException(ex);
        }

        if (isShortLink(uri)) {
            final URI rewritten = rewrite(uri);
            final RequestDispatcher requestDispatcher = request.getRequestDispatcher(rewritten.getPath());
            requestDispatcher.forward(rewrite(rewritten, httpServletRequest), response);
        } else {
            chain.doFilter(request, response);
        }

    }

    private boolean isShortLink(final URI uri) {
        return Objects.equals(uri.getHost(), shortLinkBase.getHost()) &&
                uri.getPath() != null &&
                uri.getPath().startsWith(shortLinkBase.getPath());
    }

    private URI rewrite(final URI original) throws ServletException {

        final String path = original.getPath().replaceFirst(shortLinkBase.getPath(), "");

        try {
            return new URI(
                original.getScheme(),
                original.getAuthority(),
                apiRoot + "/" + "short_link/redirection" + path,
                original.getQuery(),
                original.getFragment());
        } catch (URISyntaxException ex) {
            throw new ServletException(ex);
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

    @Override
    public void destroy() {}

    @Inject
    public void setShortLinkBase(@Named(Constants.SHORT_LINK_BASE) String shortLinkBase) {
        try {
            this.shortLinkBase = new URI(shortLinkBase.replace("/+$", ""));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
