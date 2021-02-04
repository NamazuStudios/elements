package com.namazustudios.socialengine.setup.provider;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.shell.ShellFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import static com.namazustudios.socialengine.setup.SetupSSHD.SSH_HOST;
import static com.namazustudios.socialengine.setup.SetupSSHD.SSH_PORT;

public class SshServerProvider implements Provider<SshServer> {

    private Provider<String> sshHostProvider;

    private Provider<Integer> sshPortProvider;

    private Provider<ShellFactory> shellFactoryProvider;

    private Provider<CommandFactory> commandFactoryProvider;

    @Override
    public SshServer get() {
        final var server = SshServer.setUpDefaultServer();
        server.setPort(getSshPortProvider().get());
        server.setHost(getSshHostProvider().get());
        server.setShellFactory(getShellFactoryProvider().get());
        server.setCommandFactory(getCommandFactoryProvider().get());
        return server;
    }

    public Provider<ShellFactory> getShellFactoryProvider() {
        return shellFactoryProvider;
    }

    @Inject
    public void setShellFactoryProvider(Provider<ShellFactory> shellFactoryProvider) {
        this.shellFactoryProvider = shellFactoryProvider;
    }

    public Provider<CommandFactory> getCommandFactoryProvider() {
        return commandFactoryProvider;
    }

    @Inject
    public void setCommandFactoryProvider(Provider<CommandFactory> commandFactoryProvider) {
        this.commandFactoryProvider = commandFactoryProvider;
    }

    public Provider<String> getSshHostProvider() {
        return sshHostProvider;
    }

    @Inject
    public void setSshHostProvider(@Named(SSH_HOST) Provider<String> sshHostProvider) {
        this.sshHostProvider = sshHostProvider;
    }

    public Provider<Integer> getSshPortProvider() {
        return sshPortProvider;
    }

    @Inject
    public void setSshPortProvider(@Named(SSH_PORT) Provider<Integer> sshPortProvider) {
        this.sshPortProvider = sshPortProvider;
    }

}
