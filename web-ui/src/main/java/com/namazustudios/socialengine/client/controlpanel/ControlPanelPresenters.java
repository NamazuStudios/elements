package com.namazustudios.socialengine.client.controlpanel;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.namazustudios.socialengine.client.controlpanel.view.*;
import com.namazustudios.socialengine.client.controlpanel.view.application.*;
import com.namazustudios.socialengine.client.controlpanel.view.login.LoginView;
import com.namazustudios.socialengine.client.controlpanel.view.login.LoginViewPresenter;
import com.namazustudios.socialengine.client.controlpanel.view.shortlink.ShortLinkEditorPresenter;
import com.namazustudios.socialengine.client.controlpanel.view.shortlink.ShortLinkEditorTablePresenter;
import com.namazustudios.socialengine.client.controlpanel.view.shortlink.ShortLinkEditorTableView;
import com.namazustudios.socialengine.client.controlpanel.view.shortlink.ShortLinkEditorView;
import com.namazustudios.socialengine.client.controlpanel.view.user.UserEditorPresenter;
import com.namazustudios.socialengine.client.controlpanel.view.user.UserEditorTablePresenter;
import com.namazustudios.socialengine.client.controlpanel.view.user.UserEditorTableView;
import com.namazustudios.socialengine.client.controlpanel.view.user.UserEditorView;

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
                ApplicationEditorTablePresenter.class,
                ApplicationEditorTablePresenter.MyView.class,
                ApplicationEditorTableView.class,
                ApplicationEditorTablePresenter.MyProxy.class);

        bindPresenter(
                ApplicationEditorPresenter.class,
                ApplicationEditorPresenter.MyView.class,
                ApplicationEditorView.class,
                ApplicationEditorPresenter.MyProxy.class);

        bindPresenter(
                FacebookApplicationConfigurationEditorPresenter.class,
                FacebookApplicationConfigurationEditorPresenter.MyView.class,
                FacebookApplicationConfigurationEditorView.class,
                FacebookApplicationConfigurationEditorPresenter.MyProxy.class);

        bindPresenter(
                MatchmakingApplicationConfigurationEditorPresenter.class,
                MatchmakingApplicationConfigurationEditorPresenter.MyView.class,
                MatchmakingApplicationConfigurationEditorView.class,
                MatchmakingApplicationConfigurationEditorPresenter.MyProxy.class);

        bindPresenter(
                FirebaseApplicationConfigurationEditorPresenter.class,
                FirebaseApplicationConfigurationEditorPresenter.MyView.class,
                FirebaseApplicationConfigurationEditorView.class,
                FirebaseApplicationConfigurationEditorPresenter.MyProxy.class);

    }

}
