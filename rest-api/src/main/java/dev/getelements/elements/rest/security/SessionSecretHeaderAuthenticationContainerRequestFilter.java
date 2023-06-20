//package dev.getelements.elements.rest.security;
//
//import dev.getelements.elements.security.SessionSecretHeader;
//
//import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.ContainerRequestFilter;
//import javax.ws.rs.container.PreMatching;
//import javax.ws.rs.ext.Provider;
//
///**
// * Reads the session ID f
// */
//@Provider
//@PreMatching
//public class SessionSecretHeaderAuthenticationContainerRequestFilter extends SessionIdAuthenticationContainerRequestFilter {
//
//    @Override
//    public void filter(final ContainerRequestContext requestContext) {
//        SessionSecretHeader.withValueSupplier(requestContext::getHeaderString)
//            .getSessionSecret()
//            .ifPresent(sessionId -> checkSessionAndSetAttributes(requestContext, sessionId));
//    }
//
//}
