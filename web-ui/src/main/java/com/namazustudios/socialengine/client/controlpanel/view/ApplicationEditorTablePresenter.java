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
 * Created by patricktwohig on 5/31/17.
 */
public class ApplicationEditorTablePresenter extends Presenter<ApplicationEditorTablePresenter.MyView,
                                                               ApplicationEditorTablePresenter.MyProxy> {

    public interface MyView extends View {}

    @ProxyStandard
    @NameToken(NameTokens.APPLICATION_EDIT_TABLE)
    public interface MyProxy extends ProxyPlace<ApplicationEditorTablePresenter> {}

    @Inject
    public ApplicationEditorTablePresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

}
