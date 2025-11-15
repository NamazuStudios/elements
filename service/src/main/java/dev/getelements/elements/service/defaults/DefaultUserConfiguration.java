package dev.getelements.elements.service.defaults;

import dev.getelements.elements.sdk.ElementLoader;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementEventConsumer;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Creates a default user for first time Elements setup
 */
@ElementServiceExport(DefaultUserConfiguration.class)
public class DefaultUserConfiguration {

    /**
     * The email/username to use for the default user.
     */
    @ElementDefaultAttribute(value = "root", description = "The username to use for the default user.")
    public static final String DEFAULT_USER_NAME = "dev.getelements.elements.user.default.name";

    /**
     * The email/username to use for the default user.
     */
    @ElementDefaultAttribute(value = "root@example.com", description = "The email to use for the default user.")
    public static final String DEFAULT_USER_EMAIL = "dev.getelements.elements.user.default.email";

    /**
     * The email/username to use for the default user.
     */
    @ElementDefaultAttribute(value = "example", description = "The password to use for the default user.")
    public static final String DEFAULT_USER_PASSWORD = "dev.getelements.elements.user.default.password";

    private UserDao userDao;

    private String defaultUserName;

    private String defaultUserEmail;

    private String defaultUserPassword;

    @ElementEventConsumer(ElementLoader.SYSTEM_EVENT_ELEMENT_LOADED)
    public void init() {

        final var users = getUserDao().getUsers(0,1);

        //If ANY users have been made, we assume Elements has already been set up - do not create the default user
        if(users.getTotal() > 0) {
            return;
        }

        final User user = new User();

        user.setName(getDefaultUserName());
        user.setEmail(getDefaultUserEmail());
        user.setLevel(User.Level.SUPERUSER);
        getUserDao().createUserWithPasswordStrict(user, getDefaultUserPassword());

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String getDefaultUserName() {
        return defaultUserName;
    }

    @Inject
    public void setDefaultUserName(@Named(DEFAULT_USER_NAME) String defaultUserName) {
        this.defaultUserName = defaultUserName;
    }

    public String getDefaultUserEmail() {
        return defaultUserEmail;
    }

    @Inject
    public void setDefaultUserEmail(@Named(DEFAULT_USER_EMAIL) String defaultUserEmail) {
        this.defaultUserEmail = defaultUserEmail;
    }

    public String getDefaultUserPassword() {
        return defaultUserPassword;
    }

    @Inject
    public void setDefaultUserPassword(@Named(DEFAULT_USER_PASSWORD) String defaultUserPassword) {
        this.defaultUserPassword = defaultUserPassword;
    }

}
