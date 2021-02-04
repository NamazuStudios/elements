package com.namazustudios.socialengine.setup.provider;

import org.apache.sshd.common.keyprovider.KeyPairProvider;
import org.apache.sshd.common.util.security.bouncycastle.BouncyCastleGeneratorHostKeyProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.nio.file.Paths;

import static com.namazustudios.socialengine.setup.SetupSSHD.HOST_KEY;

public class BouncyCastleGeneratorHostKeyProviderProvider implements Provider<KeyPairProvider> {

    private Provider<String> hostKeyPairPathProvider;

    @Override
    public BouncyCastleGeneratorHostKeyProvider get() {
        final var path = Paths.get(getHostKeyPairPathProvider().get());
        return new BouncyCastleGeneratorHostKeyProvider(path);
    }

    public Provider<String> getHostKeyPairPathProvider() {
        return hostKeyPairPathProvider;
    }

    @Inject
    public void setHostKeyPairPathProvider(@Named(HOST_KEY) Provider<String> hostKeyPairPathProvider) {
        this.hostKeyPairPathProvider = hostKeyPairPathProvider;
    }

}
