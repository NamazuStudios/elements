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
 * Created by patricktwohig on 6/25/15.
 */
public class ShortLinkEditorTablePresenter extends Presenter<ShortLinkEditorTablePresenter.MyView,
                                                             ShortLinkEditorTablePresenter.MyProxy> {

    public interface MyView extends View {}

    @ProxyCodeSplit
    @NameToken(NameTokens.SHORT_LINK_EDIT_TABLE)
    public interface MyProxy extends ProxyPlace<ShortLinkEditorTablePresenter> {}

    @Inject
    public ShortLinkEditorTablePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

}
