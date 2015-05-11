package com.namazustudios.socialengine;

import com.namazustudios.socialengine.dao.UserDao;
import joptsimple.OptionSet;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class UpdateUser extends AbstractUserCommand {

    @Inject
    private UserDao userDao;

    @Override
    protected void writeUserToDatabase(OptionSet optionSet) {

        final boolean strict = optionSet.has(getStrictOptionSpec());
        final boolean hasPassword = optionSet.has(getPasswordOptionSpec());

        if (hasPassword) {
            if (strict) {
                userDao.updateUserStrict(getUser(), getPassword());
            } else {
                userDao.updateActiveUser(getUser(), getPassword());
            }
        } else {
            if (strict) {
                userDao.updateUserStrict(getUser());
            } else {
                userDao.updateActiveUser(getUser());
            }
        }

        // Validate that we can get both the username and password
        userDao.validateActiveUserPassword(getUser().getName(), getPassword());
        userDao.validateActiveUserPassword(getUser().getEmail(), getPassword());

    }

}
