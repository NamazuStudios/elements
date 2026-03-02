package ${package}.service

import dev.getelements.elements.sdk.model.user.User
import dev.getelements.elements.sdk.service.user.UserService
import jakarta.inject.Inject

class GreetingServiceImpl : GreetingService {

    private lateinit var userService: UserService

    @Inject
    fun setUserService(userService: UserService) {
        this.userService = userService
    }

    override fun getGreeting(): String {
        // Because we set the dev.getelements.elements.auth.enabled attribute to "true" in the HelloWorldApplication,
        // the UserService will be automatically injected with the current user. This will apply an authentication
        // filter to every request and every service that is used in this application.
        val currentUser: User = userService.getCurrentUser()
        val isLoggedIn = currentUser.level != User.Level.UNPRIVILEGED
        val name = if (isLoggedIn) currentUser.name else "Guest"
        return "Hello, $name!"
    }

}
