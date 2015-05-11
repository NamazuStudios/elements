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

        if (optionSet.valueOf(getStrictOptionSpec())) {
            userDao.updateUserStrict(getUser());
        } else {
            userDao.updateActiveUser(getUser());
            userDao.updateActiveUserPassword(getUser().getName(), getPassword());
        }

        // Validate that we can get both the username and password
        userDao.validateActiveUserPassword(getUser().getName(), getPassword());
        userDao.validateActiveUserPassword(getUser().getEmail(), getPassword());

    }

}
