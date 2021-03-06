package com.namazustudios.socialengine.setup.provider;

import org.apache.sshd.common.keyprovider.FileHostKeyCertificateProvider;
import org.apache.sshd.common.keyprovider.HostKeyCertificateProvider;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import java.nio.file.Paths;

import static com.namazustudios.socialengine.setup.SetupSSHD.HOST_CERTIFICATE;

public class FileHostKeyCertificateProviderProvider implements Provider<HostKeyCertificateProvider> {

    private Provider<String> hostKeyCertificatePathProvider;

    @Override
    public HostKeyCertificateProvider get() {
        final var path = Paths.get(getHostKeyCertificatePathProvider().get());
        return new FileHostKeyCertificateProvider(path);
    }

    public Provider<String> getHostKeyCertificatePathProvider() {
        return hostKeyCertificatePathProvider;
    }

    @Inject
    public void setHostKeyCertificatePathProvider(@Named(HOST_CERTIFICATE) Provider<String> hostKeyCertificatePathProvider) {
        this.hostKeyCertificatePathProvider = hostKeyCertificatePathProvider;
    }

}
