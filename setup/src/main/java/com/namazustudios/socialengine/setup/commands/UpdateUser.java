package com.namazustudios.socialengine.setup.commands;

import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.model.user.User;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class UpdateUser extends AbstractUserSetupCommand {

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
        final var user = super.readOptions(optionSet);
        user.setId(optionSet.valueOf(getUserIdOptionSpec()));
        return user;
    }

    @Override
    protected void writeUserToDatabase(OptionSet optionSet) {

        final var strict = optionSet.has(getStrictOptionSpec());

        if (hasPassword()) {
            if (strict) {
                getUserDao().updateUserStrict(getUser(), getPassword());
            } else {
                getUserDao().updateActiveUser(getUser(), getPassword());
            }
        } else {
            if (strict) {
                getUserDao().updateUserStrict(getUser());
            } else {
                getUserDao().updateActiveUser(getUser());
            }
        }

        // Validate that we can get both the username and password
        getUserDao().validateActiveUserPassword(getUser().getName(), getPassword());
        getUserDao().validateActiveUserPassword(getUser().getEmail(), getPassword());

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
