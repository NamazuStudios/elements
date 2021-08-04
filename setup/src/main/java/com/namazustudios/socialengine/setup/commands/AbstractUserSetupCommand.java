package com.namazustudios.socialengine.setup.commands;

import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.exception.ValidationFailureException;
import com.namazustudios.socialengine.exception.user.UserNotFoundException;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.setup.ConsoleException;
import com.namazustudios.socialengine.setup.SecureReader;
import com.namazustudios.socialengine.setup.SetupCommand;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.jline.terminal.Terminal;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.PrintWriter;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

/**
 * Created by patricktwohig on 5/8/15.
 */
public abstract class AbstractUserSetupCommand implements SetupCommand {

    @Inject
    private Terminal terminal;

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
                .map(Enum::toString)
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

    public boolean hasPassword() {
        return !isNullOrEmpty(password);
    }

    public void run(String[] args) throws Exception {

        final OptionSet optionSet;

        try {
            optionSet = optionParser.parse(args);
            user = readOptions(optionSet);
        } catch (OptionException ex) {
            terminal.writer().println("Invalid option: " + ex.getMessage());
            optionParser.printHelpOn(terminal.writer());
            return;
        } catch (ConsoleException ex) {
            terminal.writer().printf("\nFailed to Read Input: %s\n\n", ex.getMessage());
            optionParser.printHelpOn(terminal.writer());
            return;
        }

        try {
            writeUserToDatabase(optionSet);
        } catch (ValidationFailureException ex) {

            terminal.writer().println("Encountered validation failures: " + ex.getMessage());

            for (final var failure : ex.getConstraintViolations()) {
                terminal.writer().println(failure.getPropertyPath() + " - " + failure.getMessage());
            }

        } catch (ForbiddenException ex) {
            terminal.writer().printf("Failed check user credentials after adding user: %s\n", ex.getMessage());
        } catch (Exception ex) {
            terminal.writer().printf("Failed to add user: %s\n", ex.getMessage());
            optionParser.printHelpOn(terminal.writer());
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

        final var user = new User();

        user.setName(optionSet.valueOf(usernameOptionSpec));
        user.setEmail(optionSet.valueOf(emailOptionSpec));
        user.setLevel(optionSet.valueOf(levelOptionSpec));
        user.setActive(true);

        while (isNullOrEmpty(user.getName())) {
            final var name = secureReader.read("Enter Username: ");
            user.setName(name);
        }

        while (isNullOrEmpty(user.getEmail())) {
            final var email = secureReader.read("Enter Email for %s: ", user.getName());
            user.setEmail(email);
        }

        final var levels = Stream.of(User.Level.values())
            .map(User.Level::toString)
            .collect(joining(","));

        while (user.getLevel() == null) {

            final var input = secureReader.read("User Level for \"%s\" <%s> [%s]: ",
                user.getName(),
                user.getEmail(),
                levels).toUpperCase();

            try {
                final var level = User.Level.valueOf(input);
                user.setLevel(level);
            } catch (IllegalArgumentException ex) {
                terminal.writer().println("Invalid User Level: " + input);
            }

        }

        if (optionSet.has(passwordOptionSpec)) {
            password = optionSet.valueOf(passwordOptionSpec).trim();
        }

        if (!hasPassword()) {
            password = secureReader.reads("Please enter password for user \"%s\" <%s> (blank for no update): ",
                user.getName(),
                user.getEmail()
            ).trim();
        }

        return user;

    }

    /**
     * Performs the actual changes and commits them to the database.
     * @param optionSet
     */
    protected abstract void writeUserToDatabase(OptionSet optionSet);

}
