package com.namazustudios.socialengine.setup.guice;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.setup.SetupShellFactory;
import com.namazustudios.socialengine.setup.provider.BouncyCastleGeneratorHostKeyProviderProvider;
import com.namazustudios.socialengine.setup.provider.DefaultAuthorizedKeysAuthenticatorProvider;
import com.namazustudios.socialengine.setup.provider.FileHostKeyCertificateProviderProvider;
import com.namazustudios.socialengine.setup.provider.SshServerProvider;
import org.apache.sshd.common.keyprovider.HostKeyCertificateProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.shell.ShellFactory;
import org.apache.sshd.server.shell.UnknownCommandFactory;

public class SshServerModule extends PrivateModule {
    @Override
    protected void configure() {

        // Supporting types
        bind(ShellFactory.class).to(SetupShellFactory.class);
        bind(CommandFactory.class).toInstance(UnknownCommandFactory.INSTANCE);
        bind(KeyPairProvider.class).toProvider(BouncyCastleGeneratorHostKeyProviderProvider.class);
        bind(HostKeyCertificateProvider.class).toProvider(FileHostKeyCertificateProviderProvider.class);
        bind(PublickeyAuthenticator.class).toProvider(DefaultAuthorizedKeysAuthenticatorProvider.class);

        // The main shell command
        bind(Command.class).to(InjectorScopedShellCommand.class);

        // The server itself.
        bind(SshServer.class).toProvider(SshServerProvider.class).asEagerSingleton();

        expose(SshServer.class);

    }
}
