package dev.getelements.elements.setup.commands;

import dev.getelements.elements.dao.UserDao;
import joptsimple.OptionSet;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/8/15.
 */
public class AddUser extends AbstractUserSetupCommand {

    private UserDao userDao;

    @Override
    protected void writeUserToDatabase(final OptionSet optionSet) {

        final boolean strict = optionSet.has(getStrictOptionSpec());

        if (hasPassword()) {
            if (strict) {
                getUserDao().createUserWithPasswordStrict(getUser(), getPassword());
            } else {
                getUserDao().createOrReactivateUserWithPassword(getUser(), getPassword());
            }
        } else {
            if (strict) {
                getUserDao().createUserStrict(getUser());
            } else {
                getUserDao().createOrReactivateUser(getUser());
            }
        }

        if (hasPassword()) {
            // Validate that we can get both the username and password
            getUserDao().validateActiveUserPassword(getUser().getName(), getPassword());
            getUserDao().validateActiveUserPassword(getUser().getEmail(), getPassword());
        }

    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(final UserDao userDao) {
        this.userDao = userDao;
    }

}
