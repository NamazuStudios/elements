#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.user.UserService;
import jakarta.inject.Inject;


@ElementServiceExport(GreetingService.class)
public class GreetingServiceImpl implements GreetingService {

    private UserService userService;

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getGreeting() {
        // Because we set the dev.getelements.elements.auth.enabled attribute to "true" in the HelloWorldApplication,
        // the UserService will be automatically injected with the current user. This will apply an authentication
        // filter to every request and every service that is used in this application.
        final User currentUser = userService.getCurrentUser();
        final boolean isLoggedIn = !User.Level.UNPRIVILEGED.equals(currentUser.getLevel());
        final String name = isLoggedIn ? currentUser.getName() : "Guest";

        return "Hello, " + name + "!";
    }
}
