package com.namazustudios.socialengine.service.blockchain;

import javax.inject.Inject;
import javax.inject.Provider;

public class Neow3jClientProvider implements Provider<Neow3Client> {

    private Provider<UserNeow3Client> userNeow3jClient;

    @Override
    public Neow3Client get() {
        return getUserNeow3jClient().get();
    }


    public Provider<UserNeow3Client> getUserNeow3jClient() {
        return userNeow3jClient;
    }

    @Inject
    public void setSuperUserNeow3jClient(Provider<UserNeow3Client> userNeow3jClient) {
        this.userNeow3jClient = userNeow3jClient;
    }
}
