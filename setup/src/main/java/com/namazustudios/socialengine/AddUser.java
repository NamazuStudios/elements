package com.namazustudios.socialengine;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.UserDao;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.model.User;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/8/15.
 */
public class AddUser implements Command {

    // Command line options

    private final OptionParser optionParser = new OptionParser();

    private OptionSpec<String> usernameOptionSpec;

    private OptionSpec<String> passwordOptionSpec;

    private OptionSpec<String> emailOptionSpec;

    private OptionSpec<User.Level> levelOptionSpec;

    // The data models to use

    private User user;

    private String password;

    @Inject
    private UserDao userDao;

    @Inject
    private Setup setup;

    public AddUser() {

        usernameOptionSpec = optionParser.accepts("user", "Username/Unique Identifier.  (ex. bobsmith)")
                .withRequiredArg()
                .ofType(String.class);

        emailOptionSpec = optionParser.accepts("email", "Email Address.  (ex. bobsmith@yourcompany.com)")
                .withRequiredArg()
                .ofType(String.class);

        levelOptionSpec = optionParser.accepts("level", "User Level.  One of the several predefined levels.")
                .withRequiredArg()
                .ofType(User.Level.class);

        passwordOptionSpec = optionParser.accepts("password", "Password  The user's password.")
                .withOptionalArg()
                .ofType(String.class);

    }

    public void run(String[] args) throws Exception {

        try {
            final OptionSet optionSet = optionParser.parse(args);
            readOptions(optionSet);
        } catch (OptionException ex) {
            optionParser.printHelpOn(System.err);
            return;
        } catch (Setup.ConsoleException ex) {
            System.err.println(ex.getMessage());
            optionParser.printHelpOn(System.err);
        }

        try {
            writeUserToDatabase();
        } catch (Exception ex) {
            optionParser.printHelpOn(System.err);
            throw ex;
        }

    }


    private void readOptions(final OptionSet optionSet) {

        user = new User();
        user.setName(optionSet.valueOf(usernameOptionSpec));
        user.setEmail(optionSet.valueOf(emailOptionSpec));
        user.setLevel(optionSet.valueOf(levelOptionSpec));
        user.setActive(true);

        password = optionSet.valueOf(passwordOptionSpec);

        if (Strings.isNullOrEmpty(password)) {
            password = setup.reads("Please enter root password: ");
        }

    }

    private void writeUserToDatabase() {

        // Creates or updates the user.

        try {
            userDao.createUser(user);
        } catch (DuplicateException ex) {
            userDao.updateUser(user);
        }

        userDao.updateUserPassword(user.getName(), password);

        // Validate that we can get both the username and password
        userDao.validateUserPassword(user.getName(), password);
        userDao.validateUserPassword(user.getEmail(), password);

    }

}
