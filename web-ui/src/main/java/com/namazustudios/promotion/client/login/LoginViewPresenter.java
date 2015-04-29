package com.namazustudios.promotion.client.login;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.Proxy;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 4/29/15.
 */
public class LoginViewPresenter extends Presenter<LoginViewPresenter.MyView, LoginViewPresenter.MyProxy> {

    @ProxyStandard
    public interface MyProxy extends Proxy<LoginViewPresenter> {}

    public interface MyView extends View {}

    @Inject
    public LoginViewPresenter(final EventBus eventBus,
                         final MyView view,
                         final MyProxy proxy) {
        super(eventBus, view, proxy, RevealType.Root);
    }

}
