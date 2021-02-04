package com.namazustudios.socialengine.setup;

import org.apache.sshd.server.channel.ChannelSession;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.shell.ShellFactory;

import java.io.IOException;

public class SetupShellFactory implements ShellFactory {

    @Override
    public Command createShell(final ChannelSession channel) throws IOException {
        return null;
    }

}
