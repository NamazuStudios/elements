//package com.namazustudios.socialengine.service.auth;
//
//import com.namazustudios.socialengine.model.User;
//import com.namazustudios.socialengine.service.SessionService;
//
//import javax.inject.Inject;
//import javax.inject.Provider;
//
//public class SessionServiceProvider implements Provider<SessionService> {
//
//    private User user;
//
//    private Provider<AnonSessionService> anonSessionServiceProvider;
//
//    private Provider<UserSessionService> userSessionServiceProvider;
//
//    @Override
//    public SessionService get() {
//        switch (getUser().getLevel()) {
//            case UNPRIVILEGED:
//                return getAnonSessionServiceProvider().get();
//            default:
//                return getUserSessionServiceProvider().get();
//        }
//    }
//
//    public User getUser() {
//        return user;
//    }
//
//    @Inject
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public Provider<AnonSessionService> getAnonSessionServiceProvider() {
//        return anonSessionServiceProvider;
//    }
//
//    @Inject
//    public void setAnonSessionServiceProvider(Provider<AnonSessionService> anonSessionServiceProvider) {
//        this.anonSessionServiceProvider = anonSessionServiceProvider;
//    }
//
//    public Provider<UserSessionService> getUserSessionServiceProvider() {
//        return userSessionServiceProvider;
//    }
//
//    @Inject
//    public void setUserSessionServiceProvider(Provider<UserSessionService> userSessionServiceProvider) {
//        this.userSessionServiceProvider = userSessionServiceProvider;
//    }
//}
