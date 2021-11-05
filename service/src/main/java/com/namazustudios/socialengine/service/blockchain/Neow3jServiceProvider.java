package com.namazustudios.socialengine.service.blockchain;

import javax.inject.Inject;
import javax.inject.Provider;

public class Neow3jServiceProvider implements Provider<Neow3jService> {

    private Provider<UserNeow3jService> userNeow3jService;

    @Override
    public Neow3jService get() {
        return getUserNeow3jService().get();
    }


    public Provider<UserNeow3jService> getUserNeow3jService() {
        return userNeow3jService;
    }

    @Inject
    public void setSuperUserNeow3jService(Provider<UserNeow3jService> userNeow3jService) {
        this.userNeow3jService = userNeow3jService;
    }
}
