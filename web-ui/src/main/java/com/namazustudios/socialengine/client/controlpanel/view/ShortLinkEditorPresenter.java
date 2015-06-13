package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/12/15.
 */
public class ShortLinkEditorPresenter extends Presenter<ShortLinkEditorPresenter.MyView, ShortLinkEditorPresenter.MyProxy> {

    public interface MyView extends View {}

    @ProxyStandard
    @NameToken(NameTokens.SHORT_LINK_EDIT)
    public interface MyProxy extends ProxyPlace<ShortLinkEditorPresenter> {}

    @Inject
    public ShortLinkEditorPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
        super(eventBus, view, proxy, RevealType.Root);
    }

}
