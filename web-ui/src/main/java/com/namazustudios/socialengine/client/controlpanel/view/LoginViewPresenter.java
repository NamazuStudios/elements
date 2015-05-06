package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.rest.service.LoginService;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Created by patricktwohig on 4/29/15.
 */
public class LoginViewPresenter extends Presenter<LoginViewPresenter.MyView, LoginViewPresenter.MyProxy> {

    public static final String REFRESH = "refresh";

    @Inject
    private LoginService loginService;

    @Inject
    private Provider<ControlPanelPresenter> controlPanelPresenterProvider;

    @ProxyStandard
    @NameToken(NameTokens.LOGIN)
    public interface MyProxy extends ProxyPlace<LoginViewPresenter> {}

    public interface MyView extends View {}

    @Inject
    public LoginViewPresenter(final EventBus eventBus,
                         final MyView view,
                         final MyProxy proxy) {
        super(eventBus, view, proxy, RevealType.Root);
    }

    @Override
    public boolean useManualReveal() {
        return  true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {

        super.prepareFromRequest(request);

        if (Boolean.parseBoolean(request.getParameter(REFRESH, Boolean.TRUE.toString()))) {
            loginService.refreshCurrentUser(new MethodCallback<User>() {
                @Override
                public void onFailure(Method method, Throwable throwable) {
                    revealFailure();
                }

                @Override
                public void onSuccess(Method method, User user) {
                    if (method.getResponse().getStatusCode() == 200) {
                        getProxy().manualReveal(controlPanelPresenterProvider.get());
                    } else {
                        revealFailure();
                    }
                }

                private void revealFailure() {
                    getProxy().manualReveal(LoginViewPresenter.this);
                }

            });
        } else {
            getProxy().manualReveal(this);
        }

    }

}
