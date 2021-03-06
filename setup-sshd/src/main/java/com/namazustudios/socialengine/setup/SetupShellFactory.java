package com.namazustudios.socialengine.setup;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;

public class SetupShellFactory implements ShellFactory {

    private Provider<Command> commandProvider;

    @Override
    public Command createShell(final ChannelSession channel) throws IOException {
        return getCommandProvider().get();
    }

    public Provider<Command> getCommandProvider() {
        return commandProvider;
    }

    @Inject
    public void setCommandProvider(Provider<Command> commandProvider) {
        this.commandProvider = commandProvider;
    }

}
