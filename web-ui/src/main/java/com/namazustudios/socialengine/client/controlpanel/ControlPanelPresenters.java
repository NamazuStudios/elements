package com.namazustudios.socialengine.client.controlpanel;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.namazustudios.socialengine.client.controlpanel.view.*;

/**
 * Created by patricktwohig on 4/28/15.
 */
public class ControlPanelPresenters extends AbstractPresenterModule {

    @Override
    protected void configure() {

        bindPresenter(
                LoginViewPresenter.class,
                LoginViewPresenter.MyView.class,
                LoginView.class,
                LoginViewPresenter.MyProxy.class);

        bindPresenter(
                ControlPanelPresenter.class,
                ControlPanelPresenter.MyView.class,
                ControlPanelView.class,
                ControlPanelPresenter.MyProxy.class);

        bindPresenter(
                UserEditorPresenter.class,
                UserEditorPresenter.MyView.class,
                UserEditorView.class,
                UserEditorPresenter.MyProxy.class);

        bindPresenter(
                UserEditorTablePresenter.class,
                UserEditorTablePresenter.MyView.class,
                UserEditorTableView.class,
                UserEditorTablePresenter.MyProxy.class);

        bindPresenter(
                ShortLinkEditorPresenter.class,
                ShortLinkEditorPresenter.MyView.class,
                ShortLinkEditorView.class,
                ShortLinkEditorPresenter.MyProxy.class);

        bindPresenter(
                ShortLinkEditorTablePresenter.class,
                ShortLinkEditorTablePresenter.MyView.class,
                ShortLinkEditorTableView.class,
                ShortLinkEditorTablePresenter.MyProxy.class);

        bindPresenter(
                ApplicationProfileEditorTablePresenter.class,
                ApplicationProfileEditorTablePresenter.MyView.class,
                ApplicationProfileEditorTableView.class,
                ApplicationProfileEditorTablePresenter.MyProxy.class);

    }

}
