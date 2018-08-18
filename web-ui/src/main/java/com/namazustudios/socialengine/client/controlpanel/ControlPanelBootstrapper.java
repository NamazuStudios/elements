package com.namazustudios.socialengine.client.controlpanel;

import com.gwtplatform.mvp.client.Bootstrapper;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.namazustudios.socialengine.client.rest.client.internal.UiConfigClient;
import com.namazustudios.socialengine.client.rest.service.LoginService;
import com.namazustudios.socialengine.model.UiConfig;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.session.SessionCreation;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

import static com.namazustudios.socialengine.GameOnConstants.GAMEON_ADMIN_SERVICE_ROOT;
import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static org.fusesource.restygwt.client.Defaults.setDispatcher;
import static org.fusesource.restygwt.client.Defaults.setServiceRoot;
import static org.fusesource.restygwt.client.ServiceRoots.add;

/**
 * Created by patricktwohig on 5/4/15.
 */
public class ControlPanelBootstrapper implements Bootstrapper {

    @Inject
    private PlaceManager placeManager;

    @Inject
    private UiConfigClient uiConfigClient;

    @Inject
    private LoginService loginService;

    @Override
    public void onBootstrap() {

        setServiceRoot("ui");
        setDispatcher((method, builder) -> {

            final SessionCreation sessionCreation = loginService.getSessionCreation();

            if (sessionCreation != null) {
                builder.setHeader(SESSION_SECRET, sessionCreation.getSessionSecret());
            }

            builder.setIncludeCredentials(true);

            return builder.send();

        });

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
