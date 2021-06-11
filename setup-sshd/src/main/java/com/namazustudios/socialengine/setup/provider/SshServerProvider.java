package com.namazustudios.socialengine.setup.provider;

import com.namazustudios.socialengine.setup.Deny;
import org.apache.sshd.common.cipher.BuiltinCiphers;
import org.apache.sshd.common.keyprovider.HostKeyCertificateProvider;
import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.session.SessionHeartbeatController;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.command.CommandFactory;
import org.apache.sshd.server.shell.ShellFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.namazustudios.socialengine.setup.SetupSSHD.SSH_HOST;
import static com.namazustudios.socialengine.setup.SetupSSHD.SSH_PORT;
import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.sshd.common.cipher.BuiltinCiphers.*;
import static org.apache.sshd.common.session.SessionHeartbeatController.HeartbeatType.IGNORE;

public class SshServerProvider implements Provider<SshServer> {

    private Provider<String> sshHostProvider;

    private Provider<Integer> sshPortProvider;

    private Provider<ShellFactory> shellFactoryProvider;

    private Provider<KeyPairProvider> keyPairProviderProvider;

    private Provider<HostKeyCertificateProvider> hostKeyCertificateProviderProvider;

    private Provider<PublickeyAuthenticator> publickeyAuthenticatorProvider;

    @Override
    public SshServer get() {
        final var server = SshServer.setUpDefaultServer();

        final var host = getSshHostProvider().get().trim();
        if (!host.isBlank()) server.setHost(host);

        server.setPort(getSshPortProvider().get());
        server.setShellFactory(getShellFactoryProvider().get());
        server.setKeyPairProvider(getKeyPairProviderProvider().get());
//        server.setHostKeyCertificateProvider(getHostKeyCertificateProviderProvider().get());
        server.setPublickeyAuthenticator(getPublickeyAuthenticatorProvider().get());

//        final var deny = new Deny();
//        server.setPasswordAuthenticator(deny);
//        server.setKeyboardInteractiveAuthenticator(deny);

        server.setSessionHeartbeat(IGNORE, SECONDS, 15);
        return server;
    }

    public Provider<ShellFactory> getShellFactoryProvider() {
        return shellFactoryProvider;
    }

    @Inject
    public void setShellFactoryProvider(Provider<ShellFactory> shellFactoryProvider) {
        this.shellFactoryProvider = shellFactoryProvider;
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

    public Provider<KeyPairProvider> getKeyPairProviderProvider() {
        return keyPairProviderProvider;
    }

    @Inject
    public void setKeyPairProviderProvider(Provider<KeyPairProvider> keyPairProviderProvider) {
        this.keyPairProviderProvider = keyPairProviderProvider;
    }

    public Provider<HostKeyCertificateProvider> getHostKeyCertificateProviderProvider() {
        return hostKeyCertificateProviderProvider;
    }

    @Inject
    public void setHostKeyCertificateProviderProvider(Provider<HostKeyCertificateProvider> hostKeyCertificateProviderProvider) {
        this.hostKeyCertificateProviderProvider = hostKeyCertificateProviderProvider;
    }

    public Provider<PublickeyAuthenticator> getPublickeyAuthenticatorProvider() {
        return publickeyAuthenticatorProvider;
    }

    @Inject
    public void setPublickeyAuthenticatorProvider(Provider<PublickeyAuthenticator> publickeyAuthenticatorProvider) {
        this.publickeyAuthenticatorProvider = publickeyAuthenticatorProvider;
    }

}
