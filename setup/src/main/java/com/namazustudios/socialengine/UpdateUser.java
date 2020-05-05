package com.namazustudios.socialengine;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.user.User;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class UpdateUser extends AbstractUserCommand {

    @Inject
    private UserDao userDao;

    private final OptionSpec<String> userIdOptionSpec;

    public UpdateUser() {
        userIdOptionSpec = getOptionParser().accepts("id", "The User's Unique ID.")
            .withOptionalArg()
            .ofType(String.class);
    }

    @Override
    protected User readOptions(final OptionSet optionSet) {
        final User user = super.readOptions(optionSet);
        user.setId(optionSet.valueOf(getUserIdOptionSpec()));
        return user;
    }

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

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public OptionSpec<String> getUserIdOptionSpec() {
        return userIdOptionSpec;
    }

}
