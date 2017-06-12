package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
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
 * Created by patricktwohig on 5/1/15.
 */
public class ControlPanelPresenter extends Presenter<ControlPanelPresenter.MyView, ControlPanelPresenter.MyProxy> {

    public static final NestedSlot SET_MAIN_CONTENT_TYPE = new NestedSlot();

    @ProxyStandard
    @NameToken(NameTokens.MAIN)
    public interface MyProxy extends ProxyPlace<ControlPanelPresenter> {}

    public interface MyView extends View {}

    @Inject
    private LoginService loginService;

    @Inject
    private Provider<LoginViewPresenter> loginViewPresenterProvider;

    @Inject
    public ControlPanelPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, RevealType.Root);
        History.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                Window.scrollTo(0, 0);
            }

        });
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {

        super.prepareFromRequest(request);

        loginService.refreshCurrentUser(new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                getProxy().manualReveal(loginViewPresenterProvider.get());
            }

            @Override
            public void onSuccess(Method method, User user) {
                getProxy().manualReveal(ControlPanelPresenter.this);
            }

        });

    }

}
