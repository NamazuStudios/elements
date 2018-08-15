package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.rest.client.internal.GameOnApplicationConfigurationClient;
import com.namazustudios.socialengine.client.util.PlaceRequestParameters;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

import static com.namazustudios.socialengine.client.controlpanel.view.ControlPanelPresenter.SET_MAIN_CONTENT_TYPE;

public class PrizeEditorPresenter extends Presenter<PrizeEditorPresenter.MyView, PrizeEditorPresenter.MyProxy> {

    @Inject
    private GameOnApplicationConfigurationClient gameOnApplicationConfigurationClient;

    public interface MyView extends View {

        void reset();

        void create(GameOnApplicationConfiguration gameOnApplicationConfiguration);

    }

    public interface MyProxy extends ProxyPlace<PrizeEditorPresenter> {

    }

    public PrizeEditorPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, SET_MAIN_CONTENT_TYPE);
    }

    @Override
    public void prepareFromRequest(final PlaceRequest request) {

        final PlaceRequestParameters<Parameter> parameters = PlaceRequestParameters
            .builder(Parameter.class)
            .require(Parameter.application_id)
            .require(Parameter.configuration_id)
            .build(request);

        getView().reset();

        if (!parameters.check(Parameter.application_id)) {
            Notify.notify("Application not specified.");
        } else if (!parameters.check(Parameter.configuration_id)) {
            Notify.notify("Configuration not specified.");
        } else {
            final String applicationId = parameters.get(Parameter.application_id);
            final String configurationId = parameters.get(Parameter.configuration_id);
            loadGameOnApplicationConfiguration(applicationId, configurationId);
        }

    }

    private void loadGameOnApplicationConfiguration(final String applicationId, final String configurationId) {
        gameOnApplicationConfigurationClient.getApplicationConfiguration(applicationId, configurationId,
                new MethodCallback<GameOnApplicationConfiguration>() {

            @Override
            public void onFailure(final Method method,
                                  final Throwable exception) {
                Notify.notify("Could not load Application " + method.getData());
            }

            @Override
            public void onSuccess(final Method method,
                                  final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
                getView().create(gameOnApplicationConfiguration);
            }

        });
    }

    private enum Parameter {

        /**
         * The application ID of the {@link Application} used to edit the underlying prize.
         */
        application_id,

        /**
         * The configuration ID of the {@link GameOnApplicationConfiguration} used to edit the unederlying prize.
         */
        configuration_id

    }

}
