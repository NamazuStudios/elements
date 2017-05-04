package com.namazustudios.socialengine.client.controlpanel;

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.namazustudios.socialengine.client.rest.service.LoginService;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/4/15.
 */
public class ControlPanelBootstrapper implements Bootstrapper {

    @Inject
    private LoginService loginService;

    @Inject
    private PlaceManager placeManager;

    @Override
    public void onBootstrap() {

        // This should be done in a module, however it's not because of some
        // worker problems with Resty-GWT.  This probably should be moved
        // to a configuration or @Named field but alas this is what we've got.

        Defaults.setServiceRoot("/api");

        loginService.refreshCurrentUser(new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                placeManager.revealCurrentPlace();
            }

            @Override
            public void onSuccess(Method method, User user) {
                placeManager.revealCurrentPlace();
            }

        });

    }

}
