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

        final String apiUrl = Config.getApiRoot();
        Defaults.setServiceRoot(apiUrl);


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
