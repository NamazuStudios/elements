package com.namazustudios.promotion;

import com.google.common.base.Strings;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.promotion.dao.UserDao;
import com.namazustudios.promotion.dao.mongo.guice.MongoDaoModule;
import com.namazustudios.promotion.exception.DuplicateException;
import com.namazustudios.promotion.model.User;
import com.namazustudios.promotion.rest.guice.ConfigurationModule;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;
import java.io.Console;

/**
 * Hello world!
 *
 */
public class Setup {

    // Command line options

    private final OptionParser optionParser = new OptionParser();

    private OptionSpec<String> rootUsernameOptionSpec;

    private OptionSpec<String> rootPasswordOptionSpec;

    private OptionSpec<String> rootEmailOptionSpec;

    // The data models to use

    private User rootUser = new User();

    private String rootPassword;

    @Inject
    private UserDao userDao;

    public Setup() {

        rootUsernameOptionSpec = optionParser.accepts("root-user", "Root Username")
            .withOptionalArg()
            .ofType(String.class);

        rootPasswordOptionSpec = optionParser.accepts("root-password", "Root Password")
            .withOptionalArg()
            .ofType(String.class);

        rootEmailOptionSpec = optionParser.accepts("root-email", "Root Email Address")
            .withOptionalArg()
            .ofType(String.class);

    }

    public User getRootUser() {
        return rootUser;
    }

    public void setRootUser(User rootUser) {
        this.rootUser = rootUser;
    }

    public String getRootPassword() {
        return rootPassword;
    }

    public void setRootPassword(String rootPassword) {
        this.rootPassword = rootPassword;
    }

    public void run(final String args[]) throws Exception {

        final Injector injector = Guice.createInjector(
                new ConfigurationModule(),
                new MongoDaoModule());

        injector.injectMembers(this);

        try {
            final OptionSet optionSet = optionParser.parse(args);
            readOptions(optionSet);
        } catch (OptionException ex) {
            optionParser.printHelpOn(System.out);
            return;
        }

        writeRootUserToDatabase();

    }

    private void readOptions(final OptionSet optionSet) {

        getRootUser().setName(optionSet.valueOf(rootUsernameOptionSpec));
        getRootUser().setEmail(optionSet.valueOf(rootEmailOptionSpec));
        setRootPassword(optionSet.valueOf(rootPasswordOptionSpec));

        if (Strings.isNullOrEmpty(getRootUser().getName())) {
            rootUser.setName(read("Please enter root username: "));
        }

        if (Strings.isNullOrEmpty(rootUser.getEmail())) {
            rootUser.setEmail(read("Please enter root email: "));
        }

        if (Strings.isNullOrEmpty(rootPassword)) {
            rootPassword = reads("Please enter root password: ");
        }

        rootUser.setLevel(User.Level.SUPERUSER);

    }

    private String read(final String fmt, Object... args) {

        final Console console = System.console();

        if (console == null) {
            throw new IllegalStateException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = console.readLine(fmt, args).trim();
        } while (Strings.isNullOrEmpty(value));

        return value;

    }

    private String reads(final String fmt, Object... args) {

        final Console console = System.console();

        if (console == null) {
            throw new IllegalStateException("No console instance available.  Please pass setup params via args.");
        }

        String value;

        do {
            value = new String(console.readPassword(fmt, args)).trim();
        } while (Strings.isNullOrEmpty(value));

        return value;

    }

    private void writeRootUserToDatabase() {

        // Creates or updates the user

        try {
            userDao.createUser(rootUser);
        } catch (DuplicateException ex) {
            userDao.updateUser(rootUser);
        }

        userDao.updateUserPassword(rootUser.getName(), rootPassword);

        // Validate that we can get both the username and password
        userDao.validateUserPassword(rootUser.getName(), rootPassword);
        userDao.validateUserPassword(rootUser.getEmail(), rootPassword);

    }

    public static void main( String[] args ) throws Exception {
        final Setup setup = new Setup();
        setup.run(args);
    }

}

