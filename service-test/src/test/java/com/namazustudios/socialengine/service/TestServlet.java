package com.namazustudios.socialengine.service;

import com.google.common.net.HttpHeaders;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.charset.Charset;

public class TestServlet extends HttpServlet {

    private static final String SNAKE_PATH = "/snake";

    private static final String CAMEL_PATH = "/lcamel";

    private static final Charset CHARSET = Charset.forName("UTF-8");

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws ServletException, IOException {

        final String uri = req.getRequestURI();

        if (SNAKE_PATH.equals(uri)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            reply(resp, "{ \"test_property\" : \"foo\" }");
        } else if (CAMEL_PATH.equals(uri)) {
            resp.setStatus(HttpServletResponse.SC_OK);
            reply(resp, "{ \"testProperty\" : \"foo\" }");
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

    }

    private void reply(final HttpServletResponse resp, final String s) throws IOException {
        resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        final byte[] bytes = s.getBytes(CHARSET);
        resp.getOutputStream().write(bytes);
    }

}
