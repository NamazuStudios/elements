package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.model.application.Application;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationEditorPresenter extends Presenter<ApplicationEditorPresenter.MyView,
                                                          ApplicationEditorPresenter.MyProxy> {

    public interface MyView extends View {

        /**
         * Resets the application editor.
         */
        void reset();

        /**
         * Sets up the application editor to
         */
        void createApplication();

        /**
         * Edits the supplied application.
         *
         * @param application
         */
        void editApplication(Application application);

    }

    @ProxyStandard
    @NameToken(NameTokens.APPLICATION_EDIT)
    public interface MyProxy extends ProxyPlace<ApplicationEditorPresenter> {}

    @Inject
    public ApplicationEditorPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

}
