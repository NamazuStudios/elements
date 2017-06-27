package com.namazustudios.socialengine.client.controlpanel;

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.namazustudios.socialengine.client.rest.client.CORSRequestDispatcher;
import com.namazustudios.socialengine.client.rest.client.UiConfigClient;
import com.namazustudios.socialengine.client.rest.service.LoginService;
import com.namazustudios.socialengine.model.UiConfig;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

import static org.fusesource.restygwt.client.Defaults.setDispatcher;
import static org.fusesource.restygwt.client.Defaults.setServiceRoot;

/**
 * Created by patricktwohig on 5/4/15.
 */
public class ControlPanelBootstrapper implements Bootstrapper {

    @Inject
    private LoginService loginService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    private UiConfigClient uiConfigClient;

    @Override
    public void onBootstrap() {

        setServiceRoot("ui");
        setDispatcher(new CORSRequestDispatcher());

        uiConfigClient.getUiConfig(new MethodCallback<UiConfig>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                placeManager.revealCurrentPlace();
            }

            @Override
            public void onSuccess(Method method, UiConfig uiConfig) {
                setServiceRoot(uiConfig.getApiUrl());
                refreshCurrentUser();
            }

        });

    }

    private void refreshCurrentUser() {
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
