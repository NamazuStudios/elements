//package dev.getelements.elements.rest.security;
//
//import dev.getelements.elements.security.AuthorizationHeader;
//
//import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.PreMatching;
//import javax.ws.rs.ext.Provider;
//import java.io.IOException;
//import java.util.regex.Pattern;
//
//import static java.util.regex.Pattern.compile;
//
//@Provider
//@PreMatching
//public class BearerAuthenticationContainerRequestFilter extends SessionIdAuthenticationContainerRequestFilter {
//
//    @Override
//    public void filter(final ContainerRequestContext requestContext) throws IOException {
//        AuthorizationHeader.withValueSupplier(requestContext::getHeaderString)
//            .map(AuthorizationHeader::asBearerHeader)
//            .ifPresent(h -> checkSessionAndSetAttributes(requestContext, h.getCredentials()));
//    }
//
//}
