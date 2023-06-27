package dev.getelements.elements.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@FunctionalInterface
public interface HttpHandler {

    void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
