package dev.getelements.elements.setup.commands;

import dev.getelements.elements.setup.SetupCommand;
import dev.getelements.elements.setup.SetupCommands;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.PrintWriter;

public class Help implements SetupCommand {

    @Inject
    @Named(STDOUT)
    private PrintWriter stdout;

    @Override
    public void run(String[] args) throws Exception {

        stdout.println("Supported commands are:");

        for (SetupCommands setupCommands : SetupCommands.values()) {
            stdout.println("    " + setupCommands.commandName);
        }

        stdout.flush();

    }

}
