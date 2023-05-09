package dev.getelements.elements.setup.commands;

import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.setup.SecureReader;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * Created by patricktwohig on 5/8/15.
 */
public class UpdateUser extends AbstractUserSetupCommand {

    @Inject
    private UserDao userDao;

    @Inject
    private SecureReader secureReader;

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

        while (isNullOrEmpty(user.getId())) {

            var found = userDao.findActiveUserByNameOrEmail(user.getName());

            if (found.isEmpty()) {
                found = userDao.findActiveUserByNameOrEmail(user.getName());
            }

            if (found.isPresent()) {

                final var input = secureReader.read("Confirm update to user %s %s<%s>: (Y/n)",
                    found.get().getId(),
                    found.get().getName(),
                    found.get().getEmail()
                ).trim();

                if (isNullOrEmpty(input) || input.toLowerCase().startsWith("y")) {
                    user.setId(found.get().getId());
                }

            } else {

                final var input = secureReader.read("No user exists for %s %s<%s>. Please supply ID: ",
                    found.get().getName(),
                    found.get().getEmail()
                ).trim();

                user.setId(input);

            }

        }

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
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public OptionSpec<String> getUserIdOptionSpec() {
        return userIdOptionSpec;
    }

}
