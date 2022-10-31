package com.namazustudios.socialengine.servlet.security;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

@FunctionalInterface
interface ServletFilterProcessor<RequestT extends ServletRequest, ResponseT extends ServletResponse> {

    /**
     * Processes the request.
     *
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    void process(RequestT request, ResponseT response, FilterChain chain) throws IOException, ServletException;

}
