package com.namazustudios.socialengine;

import com.namazustudios.socialengine.dao.UserDao;
import joptsimple.OptionSet;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/8/15.
 */
public class AddUser extends AbstractUserCommand {

    @Inject
    private UserDao userDao;

    @Override
    protected void writeUserToDatabase(OptionSet optionSet) {

        if (optionSet.has(getPasswordOptionSpec())) {
            if (optionSet.valueOf(getStrictOptionSpec())) {
                userDao.createUserStrict(getUser(), getPassword());
            } else {
                userDao.createOrActivateUser(getUser(), getPassword());
            }
        } else {
            if (optionSet.valueOf(getStrictOptionSpec())) {
                userDao.createUserStrict(getUser());
            } else {
                userDao.createOrActivateUser(getUser());
            }
        }

        // Validate that we can get both the username and password
        userDao.validateActiveUserPassword(getUser().getName(), getPassword());
        userDao.validateActiveUserPassword(getUser().getEmail(), getPassword());

    }

}
