package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/5/15.
 */
public class CreateUserPresenter extends Presenter<CreateUserPresenter.MyView, CreateUserPresenter.MyProxy> {


    @ProxyCodeSplit
    @NameToken(NameTokens.USER_CREATE)
    public interface MyProxy extends ProxyPlace<CreateUserPresenter> {}

    public interface MyView extends View {}

    @Inject
    public CreateUserPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

    @Override
    protected void onReset() {
        super.onReset();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
    }
}
