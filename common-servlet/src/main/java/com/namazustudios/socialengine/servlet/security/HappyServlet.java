package com.namazustudios.socialengine.servlet.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;

/**
 * A happy little servlet.
 *
 * In order to satisfy the requirements of Kubernetes based GCP load balancers the root of all services must return a
 * 200 OK (happy) response at the root level.
 *
 * @author bobross
 */
public class HappyServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(HappyServlet.class);

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws IOException {
        if ("/".equals(req.getServletPath())) {
            resp.setStatus(SC_OK);
            write("OK", req, resp);
        } else {
            resp.setStatus(SC_NOT_FOUND);
            write("NOT FOUND", req, resp);
        }
    }

    private void write(final String message,
                       final HttpServletRequest req,
                       final HttpServletResponse resp) throws IOException {
        try {

            final var encoding = req.getCharacterEncoding();

            final var bytes = encoding == null
                ? format("%s\n", message).getBytes(UTF_8)
                : format("%s\n", message).getBytes(req.getCharacterEncoding());

            resp.getOutputStream().write(bytes);
            resp.setContentType("text/plain");

        } catch (UnsupportedEncodingException e) {
            logger.debug("Got bad charset in request.", e);
        }
    }

}
