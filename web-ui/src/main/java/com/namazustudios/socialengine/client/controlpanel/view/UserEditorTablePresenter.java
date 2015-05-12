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
 * Created by patricktwohig on 5/11/15.
 */
public class UserEditorTablePresenter extends Presenter<UserEditorTablePresenter.MyView, UserEditorPresenter.MyProxy> {

    @ProxyCodeSplit
    @NameToken(NameTokens.USER_EDIT_TABLE)
    public interface MyProxy extends ProxyPlace<UserEditorTablePresenter> {}

    public interface MyView extends View {}

    @Inject
    public UserEditorTablePresenter(
            final EventBus eventBus,
            final MyView view,
            final UserEditorPresenter.MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

}
