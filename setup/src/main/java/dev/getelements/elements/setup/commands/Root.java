package dev.getelements.elements.setup.commands;

import com.google.inject.Injector;
import dev.getelements.elements.setup.NoSuchCommandException;
import dev.getelements.elements.setup.SetupCommand;
import dev.getelements.elements.setup.SetupCommands;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.PrintWriter;
import java.util.Arrays;

public class Root implements SetupCommand {

    @Inject
    private Injector injector;

    @Inject
    @Named(STDOUT)
    private PrintWriter stdout;

    @Override
    public void run(String[] args) throws Exception {

        Class<? extends SetupCommand> commandType;

        if (args.length == 0) {
            stdout.println("Missing command.  Supported commands are:");
            commandType = SetupCommands.HELP.commandType;
        } else {
            try {
                commandType = SetupCommands.getCommandForName(args[0]);
                args = Arrays.copyOfRange(args, 1, args.length);
            } catch (NoSuchCommandException ex) {
                stdout.printf("Unknown command: %s\n", args[0]);
                commandType = SetupCommands.HELP.commandType;
                args = Arrays.copyOfRange(args, 1, args.length);
            }
        }

        try (var cmd = injector.getInstance(commandType)) {
            cmd.run(args);
        }

    }

}
