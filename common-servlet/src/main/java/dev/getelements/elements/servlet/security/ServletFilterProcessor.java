package dev.getelements.elements.servlet.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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
