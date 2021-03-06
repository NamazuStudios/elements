package com.namazustudios.socialengine.setup.commands;

import com.google.common.base.Strings;
import com.namazustudios.socialengine.setup.ConsoleException;
import com.namazustudios.socialengine.setup.SecureReader;
import com.namazustudios.socialengine.setup.SetupCommand;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import com.namazustudios.socialengine.model.user.User;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;

import java.io.PrintWriter;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Created by patricktwohig on 5/8/15.
 */
public abstract class AbstractUserSetupCommand implements SetupCommand {

    @Inject
    @Named(STDOUT)
    private PrintWriter stdout;

    @Inject
    private SecureReader secureReader;

    private final OptionParser optionParser = new OptionParser();

    private final OptionSpec<String> usernameOptionSpec;

    private final OptionSpec<String> passwordOptionSpec;

    private final OptionSpec<String> emailOptionSpec;

    private final OptionSpec<User.Level> levelOptionSpec;

    private final OptionSpec<Boolean> strictOptionSpec;

    private User user;

    private String password;

    public AbstractUserSetupCommand() {

        usernameOptionSpec = getOptionParser().accepts("user", "The user's login/username.  (ex. bobsmith)")
                .withRequiredArg()
                .ofType(String.class);

        emailOptionSpec = getOptionParser().accepts("email", "The user's email Address.  (ex. bobsmith@yourcompany.com)")
                .withRequiredArg()
                .ofType(String.class);

        passwordOptionSpec = getOptionParser().accepts("password", "The user's password.  If unset this will prompt for a value.")
                .withOptionalArg()
                .ofType(String.class);

        final String levelDescription = "The user level one of: " + stream(User.Level.values())
                .map(l -> l.toString())
                .collect(joining());

        levelOptionSpec = getOptionParser().accepts("level", levelDescription)
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
            user = readOptions(optionSet);
        } catch (OptionException ex) {
            optionParser.printHelpOn(stdout);
            return;
        } catch (ConsoleException ex) {
            stdout.printf("\nFailed to Read Input: %s\n\n", ex.getMessage());
            optionParser.printHelpOn(stdout);
            return;
        }

        try {
            writeUserToDatabase(optionSet);
        } catch (ValidationFailureException ex) {
            stdout.println("Encountered validation failures.");
            for (final ConstraintViolation<?> failure : ex.getConstraintViolations()) {
                stdout.println(failure.getPropertyPath() + " - " + failure.getMessage());
            }
        } catch (Exception ex) {
            optionParser.printHelpOn(stdout);
            throw ex;
        }

    }

    /**
     * Reads the {@link OptionSet} and generates a {@link User} instance from the options supplied to this command.
     *
     * The returned {@link User} will be made available using subsequent calls using {@link #getUser()}.
     *
     * @param optionSet the {@link OptionSet} made from the arguments passed to {@link #run(String[])}.
     */
    protected User readOptions(final OptionSet optionSet) {

        final User user = new User();

        user.setName(optionSet.valueOf(usernameOptionSpec));
        user.setEmail(optionSet.valueOf(emailOptionSpec));
        user.setLevel(optionSet.valueOf(levelOptionSpec));
        user.setActive(true);

        if (optionSet.has(passwordOptionSpec)) {
            password = optionSet.valueOf(passwordOptionSpec);
        }

        if (Strings.isNullOrEmpty(password)) {
            final String prompt = String.format("Please enter password for user %s: ", user.getEmail());
            password = secureReader.reads(prompt);
        }

        return user;
    }

    /**
     * Performs the actual changes and commits them to the database.
     * @param optionSet
     */
    protected abstract void writeUserToDatabase(OptionSet optionSet);

}
