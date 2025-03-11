package dev.getelements.elements.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@FunctionalInterface
public interface HttpHandler {

    void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

}
