package com.namazustudios.socialengine.setup.provider;

import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator;
import org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import static com.namazustudios.socialengine.setup.SetupSSHD.AUTHORIZED_KEYS;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

public class DefaultAuthorizedKeysAuthenticatorProvider implements Provider<PublickeyAuthenticator> {

    private Provider<String> authorizedKeysPathProvider;

    @Override
    public AuthorizedKeysAuthenticator get() {
        final var path = Paths.get(getAuthorizedKeysPathProvider().get());
        return new DefaultAuthorizedKeysAuthenticator(path, true, NOFOLLOW_LINKS);
    }

    public Provider<String> getAuthorizedKeysPathProvider() {
        return authorizedKeysPathProvider;
    }

    @Inject
    public void setAuthorizedKeysPathProvider(@Named(AUTHORIZED_KEYS)Provider<String> authorizedKeysPathProvider) {
        this.authorizedKeysPathProvider = authorizedKeysPathProvider;
    }

}
