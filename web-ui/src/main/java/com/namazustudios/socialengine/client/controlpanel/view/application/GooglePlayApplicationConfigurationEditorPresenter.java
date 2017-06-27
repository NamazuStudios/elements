package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.view.ControlPanelPresenter;
import com.namazustudios.socialengine.client.rest.client.GooglePlayApplicationConfigurationClient;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.namazustudios.socialengine.client.controlpanel.view.application.GooglePlayApplicationConfigurationEditorPresenter.Param.application_id;
import static com.namazustudios.socialengine.client.controlpanel.view.application.GooglePlayApplicationConfigurationEditorPresenter.Param.configuration_id;

/**
 * Created by patricktwohig on 6/16/17.
 */
public class GooglePlayApplicationConfigurationEditorPresenter extends Presenter<
        GooglePlayApplicationConfigurationEditorPresenter.MyView,
        GooglePlayApplicationConfigurationEditorPresenter.MyProxy> {

    public interface MyView extends View {

        /**
         * Resets the application editor.
         */
        void reset();

        /**
         * Sets up the application editor to
         */
        void createApplicationConfiguration();

        /**
         * Edits the supplied application.
         *
         * @param googlePlayApplicationConfiguration
         */
        void editApplicationConfiguration(GooglePlayApplicationConfiguration googlePlayApplicationConfiguration);

    }

    public interface MyProxy extends ProxyPlace<IosApplicationConfigurationEditorPresenter> {}

    @Inject
    private GooglePlayApplicationConfigurationClient googlePlayApplicationConfigurationClient;

    @Inject
    public GooglePlayApplicationConfigurationEditorPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
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
        getView().createApplicationConfiguration();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        final String applicationId = request.getParameter(application_id.name().toLowerCase(), "").trim();
        final String configurationId = request.getParameter(configuration_id.name().toLowerCase(), "").trim();

        if (isNullOrEmpty(configurationId) || isNullOrEmpty(applicationId)) {
            getView().createApplicationConfiguration();
            getProxy().manualReveal(this);
        } else {
            loadApplicationConifgurationAndShow(applicationId, configurationId);
        }

    }

    private void loadApplicationConifgurationAndShow(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {
        googlePlayApplicationConfigurationClient.getApplicationConfiguration(
                applicationNameOrId,
                applicationConfigurationNameOrId,
                new MethodCallback<GooglePlayApplicationConfiguration>() {

                    @Override
                    public void onFailure(Method method, Throwable exception) {
                        getProxy().manualReveal(GooglePlayApplicationConfigurationEditorPresenter.this);
                        getView().createApplicationConfiguration();
                        Notify.notify("Could not load application " + method.getData());
                    }

                    @Override
                    public void onSuccess(Method method, GooglePlayApplicationConfiguration response) {
                        getProxy().manualReveal(GooglePlayApplicationConfigurationEditorPresenter.this);
                        getView().editApplicationConfiguration(response);
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
