package com.namazustudios.socialengine.servlet.security;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.servlet.http.HttpServletResponse.SC_OK;

public class HappyServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest req,
                         final HttpServletResponse resp) throws IOException {
        resp.setStatus(SC_OK);
        resp.setContentType("text/plain");
        resp.getOutputStream().write("OK".getBytes(UTF_8));
    }

}
