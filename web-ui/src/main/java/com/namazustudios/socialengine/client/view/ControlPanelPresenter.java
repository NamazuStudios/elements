package com.namazustudios.socialengine.client.view;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.place.NameTokens;
import com.namazustudios.socialengine.client.rest.LoginService;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/1/15.
 */
public class ControlPanelPresenter extends Presenter<ControlPanelPresenter.MyView, ControlPanelPresenter.MyProxy> {

    @ProxyStandard
    @NameToken(NameTokens.CONTROL_PANEL)
    public interface MyProxy extends ProxyPlace<ControlPanelPresenter> {}

    public interface MyView extends View {}

    @Inject
    private LoginService loginService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    public ControlPanelPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, RevealType.Root);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {
        
    }
}
