package com.namazustudios.socialengine;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import com.namazustudios.socialengine.model.User;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;

/**
 * Created by patricktwohig on 5/8/15.
 */
public abstract class AbstractUserCommand implements Command {

    @Inject
    private Setup setup;

    private final OptionParser optionParser = new OptionParser();

    private OptionSpec<String> usernameOptionSpec;

    private OptionSpec<String> passwordOptionSpec;

    private OptionSpec<String> emailOptionSpec;

    private OptionSpec<User.Level> levelOptionSpec;

    private OptionSpec<Boolean> strictOptionSpec;

    private User user;

    private String password;

    public AbstractUserCommand() {
        usernameOptionSpec = getOptionParser().accepts("user", "Username/Unique Identity.  (ex. bobsmith)")
                .withRequiredArg()
                .ofType(String.class);
        emailOptionSpec = getOptionParser().accepts("email", "Email Address.  (ex. bobsmith@yourcompany.com)")
                .withRequiredArg()
                .ofType(String.class);
        passwordOptionSpec = getOptionParser().accepts("password", "Password  The user's password.")
                .withOptionalArg()
                .ofType(String.class);
        levelOptionSpec = getOptionParser().accepts("level", "User Level.  One of the several predefined levels.")
                .withRequiredArg()
                .ofType(User.Level.class);
        strictOptionSpec = getOptionParser().accepts("strict", "Flag to toggle strict mode.  Default true.")
                .withOptionalArg()
                .ofType(boolean.class);
    }

    public OptionParser getOptionParser() {
        return optionParser;
    }

    public OptionSpec<String> getUsernameOptionSpec() {
        return usernameOptionSpec;
    }

    public OptionSpec<String> getPasswordOptionSpec() {
        return passwordOptionSpec;
    }

    public OptionSpec<String> getEmailOptionSpec() {
        return emailOptionSpec;
    }

    public OptionSpec<User.Level> getLevelOptionSpec() {
        return levelOptionSpec;
    }

    public OptionSpec<Boolean> getStrictOptionSpec() {
        return strictOptionSpec;
    }

    public User getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public void run(String[] args) throws Exception {

        final OptionSet optionSet;

        try {
            optionSet = optionParser.parse(args);
            readOptions(optionSet);
        } catch (OptionException ex) {
            optionParser.printHelpOn(System.err);
            return;
        } catch (Setup.ConsoleException ex) {
            System.err.println(ex.getMessage());
            optionParser.printHelpOn(System.err);
            return;
        }

        try {
            writeUserToDatabase(optionSet);
        } catch (ValidationFailureException ex) {
            System.err.println("Encountered validation failures.");
            for (final ConstraintViolation<?> failure : ex.getConstraintViolations()) {
                System.err.println(failure.getPropertyPath() + " - " + failure.getMessage());
            }
        } catch (Exception ex) {
            optionParser.printHelpOn(System.err);
            throw ex;
        }

    }

    private void readOptions(final OptionSet optionSet) {

        user = new User();

        getUser().setName(optionSet.valueOf(usernameOptionSpec));
        getUser().setEmail(optionSet.valueOf(emailOptionSpec));
        getUser().setLevel(optionSet.valueOf(levelOptionSpec));
        getUser().setActive(true);

        if (optionSet.has(passwordOptionSpec)) {
            password = optionSet.valueOf(passwordOptionSpec);
        }

        if (Strings.isNullOrEmpty(password)) {
            final String prompt = String.format("Please enter password for user %s: ", user.getEmail());
            password = setup.reads(prompt);
        }

    }

    protected abstract void writeUserToDatabase(OptionSet optionSet);

}
