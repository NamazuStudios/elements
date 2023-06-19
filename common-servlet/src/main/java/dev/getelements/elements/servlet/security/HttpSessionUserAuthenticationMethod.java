//package dev.getelements.elements.servlet.security;
//
//import dev.getelements.elements.exception.ForbiddenException;
//import dev.getelements.elements.model.user.User;
//import dev.getelements.elements.security.UserAuthenticationMethod;
//
//import javax.inject.Inject;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpSession;
//
//import static dev.getelements.elements.model.user.User.USER_ATTRIBUTE;
//
///**
// * Created by patricktwohig on 6/26/17.
// */
//public class HttpSessionUserAuthenticationMethod implements UserAuthenticationMethod {
//
//    private HttpServletRequest httpServletRequest;
//
//    @Override
//    public User attempt() {
//
//        final HttpSession httpSession = getHttpServletRequest().getSession(false);
//
//        if (httpSession == null) {
//            throw new ForbiddenException();
//        }
//
//        final User user = (User) getHttpServletRequest()
//            .getSession()
//            .getAttribute(USER_ATTRIBUTE);
//
//        if (user == null) {
//            throw new ForbiddenException();
//        }
//
//        return user;
//
//    }
//
//    public HttpServletRequest getHttpServletRequest() {
//        return httpServletRequest;
//    }
//
//    @Inject
//    public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
//        this.httpServletRequest = httpServletRequest;
//    }
//
//}
