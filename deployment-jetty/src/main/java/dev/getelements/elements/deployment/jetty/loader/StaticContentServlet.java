package dev.getelements.elements.deployment.jetty.loader;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;

/**
 * A minimal {@link HttpServlet} that serves static files from a pre-computed {@link StaticServingConfig}.
 *
 * <p>All header values and MIME types are resolved once at load time by {@link StaticRuleEngine}; no per-request
 * processing beyond a map lookup is required.</p>
 *
 * <p>Supports {@code GET} and {@code HEAD} only; all other methods return {@code 405 Method Not Allowed}.</p>
 *
 * <p>Root requests ({@code /}) are served by the configured index file (default {@code index.html}). If no index
 * file is present in the content tree a {@code 404} is returned.</p>
 *
 * <p>When an error page is configured for a given HTTP status code (via
 * {@code dev.getelements.static.error.<code>}), that file is streamed as the response body instead of Jetty's
 * default error page.</p>
 */
class StaticContentServlet extends HttpServlet {

    private final StaticServingConfig config;

    StaticContentServlet(final StaticServingConfig config) {
        this.config = config;
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        serve(req, resp, true);
    }

    @Override
    protected void doHead(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        serve(req, resp, false);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        sendErrorOrPage(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, true);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        sendErrorOrPage(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, true);
    }

    @Override
    protected void doDelete(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        sendErrorOrPage(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, true);
    }

    @Override
    protected void doOptions(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setHeader("Allow", "GET, HEAD, OPTIONS");
    }

    private void serve(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final boolean sendBody) throws IOException {

        final var pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            if (config.indexFile() != null) {
                serveFile(resp, config.indexFile(), sendBody);
            } else {
                sendErrorOrPage(resp, HttpServletResponse.SC_NOT_FOUND, sendBody);
            }
            return;
        }

        // Strip leading slash
        final var relativePath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        final var meta = config.files().get(relativePath);
        if (meta == null) {
            sendErrorOrPage(resp, HttpServletResponse.SC_NOT_FOUND, sendBody);
            return;
        }

        serveFile(resp, meta, sendBody);
    }

    private void serveFile(
            final HttpServletResponse resp,
            final StaticFileMetadata meta,
            final boolean sendBody) throws IOException {

        resp.setContentType(meta.mimeType());
        meta.resolvedHeaders().forEach(resp::setHeader);

        final long size = Files.size(meta.absolutePath());
        resp.setContentLengthLong(size);

        if (sendBody) {
            try (final var in = Files.newInputStream(meta.absolutePath())) {
                in.transferTo(resp.getOutputStream());
            }
        }
    }

    private void sendErrorOrPage(
            final HttpServletResponse resp,
            final int code,
            final boolean sendBody) throws IOException {

        final var errorPage = config.errorPages().get(code);
        if (errorPage != null) {
            resp.setStatus(code);
            serveFile(resp, errorPage, sendBody);
        } else {
            resp.sendError(code);
        }
    }

}