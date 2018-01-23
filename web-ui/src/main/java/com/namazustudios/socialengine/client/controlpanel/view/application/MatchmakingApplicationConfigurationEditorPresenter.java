package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.controlpanel.view.ControlPanelPresenter;
import com.namazustudios.socialengine.client.rest.client.MatchmakingApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.MatchmakingApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.namazustudios.socialengine.client.controlpanel.view.application.MatchmakingApplicationConfigurationEditorPresenter.Param.application_id;
import static com.namazustudios.socialengine.client.controlpanel.view.application.MatchmakingApplicationConfigurationEditorPresenter.Param.configuration_id;

/**
 * Created by patricktwohig on 6/16/17.
 */
public class MatchmakingApplicationConfigurationEditorPresenter extends Presenter<
        MatchmakingApplicationConfigurationEditorPresenter.MyView,
        MatchmakingApplicationConfigurationEditorPresenter.MyProxy> {

    public interface MyView extends View {

        /**
         * Resets the application editor.
         */
        void reset();

        /**
         * Setup the view with no selection of application.
         */
        void createEmpty();

        /**
         * Sets up the application editor to create a configuration for the specified application.
         */
        void createApplicationConfiguration(String applicationNameOrId);

        /**
         * Edits the supplied application.
         *
         * @param applicationNameOrId
         * @param facebookApplicationConfiguration
         */
        void editApplicationConfiguration(
                String applicationNameOrId,
                MatchmakingApplicationConfiguration facebookApplicationConfiguration);

    }

    @ProxyCodeSplit
    @NameToken(NameTokens.APPLICATION_CONFIG_MATCHMAKING_EDIT)
    public interface MyProxy extends ProxyPlace<MatchmakingApplicationConfigurationEditorPresenter> {}

    @Inject
    private MatchmakingApplicationConfigurationClient matchmakingApplicationConfigurationClient;

    @Inject
    public MatchmakingApplicationConfigurationEditorPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().reset();
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().createEmpty();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        final String applicationId = request.getParameter(application_id.name().toLowerCase(), "").trim();
        final String configurationId = request.getParameter(configuration_id.name().toLowerCase(), "").trim();

        if (isNullOrEmpty(applicationId)) {
            getView().createEmpty();
        } else if (isNullOrEmpty(configurationId)) {
            getView().createApplicationConfiguration(applicationId);
            getProxy().manualReveal(this);
        } else {
            loadApplicationConifgurationAndShow(applicationId, configurationId);
        }

    }

    private void loadApplicationConifgurationAndShow(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        matchmakingApplicationConfigurationClient.getApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                new MethodCallback<MatchmakingApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        getProxy().manualReveal(MatchmakingApplicationConfigurationEditorPresenter.this);
                        getView().createEmpty();
                        Notify.notify("Could not load application configuration " + method.getData());
                    }

                    @Override
                    public void onSuccess(Method method, MatchmakingApplicationConfiguration response) {
                        getProxy().manualReveal(MatchmakingApplicationConfigurationEditorPresenter.this);
                        getView().editApplicationConfiguration(applicationNameOrId, response);
                    }

                });
    }

    public enum Param {

        /**
         * The parent application ID.
         */
        application_id,

        /**
         * The configuration ID.
         */
        configuration_id,

    }

}
