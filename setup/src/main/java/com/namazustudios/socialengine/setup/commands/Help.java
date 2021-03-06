package com.namazustudios.socialengine.setup.commands;

import com.namazustudios.socialengine.setup.SetupCommand;
import com.namazustudios.socialengine.setup.SetupCommands;

import javax.inject.Inject;
import javax.inject.Named;
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
