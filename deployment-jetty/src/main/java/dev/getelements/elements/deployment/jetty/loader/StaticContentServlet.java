package dev.getelements.elements.deployment.jetty.loader;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

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
 *
 * <p>Supports HTTP range requests ({@code Range: bytes=...}) for efficient partial content delivery, which
 * is particularly useful for streaming large audio and video assets. The {@code Accept-Ranges: bytes} header is
 * advertised on all file responses. Multi-range requests fall back to a full {@code 200} response. When
 * {@code If-Range} is present the request also falls back to a full {@code 200} response, as this servlet does
 * not issue or validate ETags.</p>
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
                serveFile(req, resp, config.indexFile(), sendBody);
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

        serveFile(req, resp, meta, sendBody);
    }

    private void serveFile(
            final HttpServletRequest req,
            final HttpServletResponse resp,
            final StaticFileMetadata meta,
            final boolean sendBody) throws IOException {

        resp.setContentType(meta.mimeType());
        meta.resolvedHeaders().forEach(resp::setHeader);
        resp.setHeader("Accept-Ranges", "bytes");

        final long size = Files.size(meta.absolutePath());
        final var rangeHeader = req.getHeader("Range");

        // Multi-range requests are not supported; fall through to full response.
        // If-Range is not supported (no ETag); fall through to avoid serving stale partial content.
        if (rangeHeader != null && !rangeHeader.contains(",") && req.getHeader("If-Range") == null) {
            final var range = parseRange(rangeHeader, size);

            if (range == null) {
                resp.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                resp.setHeader("Content-Range", "bytes */" + size);
                return;
            }

            final long start = range[0];
            final long end   = range[1];
            final long count = end - start + 1;

            resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            resp.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + size);
            resp.setContentLengthLong(count);

            if (sendBody) {
                try (final var ch = FileChannel.open(meta.absolutePath(), StandardOpenOption.READ)) {
                    ch.transferTo(start, count, Channels.newChannel(resp.getOutputStream()));
                }
            }

            return;
        }

        resp.setContentLengthLong(size);

        if (sendBody) {
            try (final var in = Files.newInputStream(meta.absolutePath())) {
                in.transferTo(resp.getOutputStream());
            }
        }
    }

    /**
     * Parses a single {@code Range: bytes=<start>-<end>} header and returns {@code [start, end]} inclusive,
     * clamped to {@code [0, size-1]}.
     *
     * <p>Supports open-ended ({@code bytes=500-}) and suffix ({@code bytes=-500}) forms. The caller is
     * responsible for filtering out multi-range headers before calling this method.</p>
     *
     * @return the resolved {@code [start, end]} pair, or {@code null} if the range is syntactically
     *         invalid or unsatisfiable against the given file size
     */
    private static long[] parseRange(final String header, final long size) {

        if (!header.startsWith("bytes=")) return null;

        final var spec = header.substring(6);
        final int dash = spec.indexOf('-');
        if (dash < 0) return null;

        final var startStr = spec.substring(0, dash).strip();
        final var endStr   = spec.substring(dash + 1).strip();

        try {

            final long start;
            final long end;

            if (startStr.isEmpty()) {
                // Suffix form: bytes=-N → last N bytes
                final long suffix = Long.parseLong(endStr);
                if (suffix <= 0) return null;
                start = Math.max(0, size - suffix);
                end = size - 1;
            } else {
                start = Long.parseLong(startStr);
                end   = endStr.isEmpty() ? size - 1 : Long.parseLong(endStr);
            }

            if (start < 0 || start >= size || end < start) return null;
            return new long[]{start, Math.min(end, size - 1)};

        } catch (final NumberFormatException e) {
            return null;
        }
    }

    private void sendErrorOrPage(
            final HttpServletResponse resp,
            final int code,
            final boolean sendBody) throws IOException {

        final var errorPage = config.errorPages().get(code);
        if (errorPage != null) {
            resp.setStatus(code);
            resp.setContentType(errorPage.mimeType());
            errorPage.resolvedHeaders().forEach(resp::setHeader);
            final long size = Files.size(errorPage.absolutePath());
            resp.setContentLengthLong(size);
            if (sendBody) {
                try (final var in = Files.newInputStream(errorPage.absolutePath())) {
                    in.transferTo(resp.getOutputStream());
                }
            }
        } else {
            resp.sendError(code);
        }
    }

}
