package com.namazustudios.socialengine.client.controlpanel.view.shortlink;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.controlpanel.view.ControlPanelPresenter;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/12/15.
 */
public class ShortLinkEditorPresenter extends Presenter<ShortLinkEditorPresenter.MyView, ShortLinkEditorPresenter.MyProxy> {

    public interface MyView extends View {}

    @ProxyCodeSplit
    @NameToken(NameTokens.SHORT_LINK_CREATE)
    public interface MyProxy extends ProxyPlace<ShortLinkEditorPresenter> {}

    @Inject
    public ShortLinkEditorPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

}
