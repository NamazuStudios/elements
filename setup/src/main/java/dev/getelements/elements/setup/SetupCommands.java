package dev.getelements.elements.setup;

import dev.getelements.elements.setup.commands.AddUser;
import dev.getelements.elements.setup.commands.DumpDefaultProperties;
import dev.getelements.elements.setup.commands.Help;
import dev.getelements.elements.setup.commands.UpdateUser;
import dev.getelements.elements.setup.commands.SetupShell;

import java.util.stream.Stream;

public enum SetupCommands {

    /**
     * Adds a new user to the database.
     */
    ADD_USER("add-user", AddUser.class),

    /**
     * Updates an existing user in the database.
     */
    UPDATE_USER("update-user", UpdateUser.class),

    /**
     * Dumps the default properties for the server.
     */
    DUMP_DEFAULT_PROPERTIES("dump-default-properties", DumpDefaultProperties.class),

    /**
     * Displays the overall help command.
     */
    HELP("help", Help.class),

    /**
     * Runs an interactive shell.
     */
    SHELL("shell", SetupShell.class, false);

    public final String commandName;

    public final boolean shellCommand;

    public final Class<? extends SetupCommand> commandType;

    SetupCommands(final String commandName, final Class<? extends SetupCommand> command) {
        this(commandName, command, true);
    }

    SetupCommands(final String commandName, final Class<? extends SetupCommand> command, final boolean shellCommand) {
        this.shellCommand = shellCommand;
        this.commandName = commandName;
        this.commandType = command;
    }

    public static Class<? extends SetupCommand> getCommandForName(final String name) {
        return Stream.of(values())
            .filter(c -> c.commandName.equals(name))
            .map(c -> c.commandType)
            .findFirst()
            .orElseThrow(() -> new NoSuchCommandException("Unknown command: " + name));
    }

    public static Stream<SetupCommands> streamShellCommands() {
        return Stream.of(values()).filter(c -> c.shellCommand);
    }

}
