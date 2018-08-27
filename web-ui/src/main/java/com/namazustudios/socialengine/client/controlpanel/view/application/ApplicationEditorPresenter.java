package com.namazustudios.socialengine.client.controlpanel.view.application;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.controlpanel.view.ControlPanelPresenter;
import com.namazustudios.socialengine.client.rest.client.internal.ApplicationClient;
import com.namazustudios.socialengine.model.application.Application;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

import static com.namazustudios.socialengine.client.controlpanel.view.application.ApplicationEditorPresenter.Param.application_id;

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
    private ApplicationClient applicationClient;

    @Inject
    public ApplicationEditorPresenter(
            final EventBus eventBus,
            final MyView view,
            final MyProxy proxy) {
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
        getView().createApplication();
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        final String userName = request.getParameter(application_id.name().toLowerCase(), "").trim();

        if (Strings.isNullOrEmpty(userName)) {
            getView().createApplication();
            getProxy().manualReveal(this);
        } else {
            loadApplicationAndShow(userName);
        }

    }

    private void loadApplicationAndShow(final String applicationNameOrId) {
        applicationClient.getApplication(applicationNameOrId, new MethodCallback<Application>() {

            @Override
            public void onFailure(Method method, Throwable exception) {
                getProxy().manualReveal(ApplicationEditorPresenter.this);
                getView().createApplication();
                Notify.notify("Could not load application " + method.getData());

            }

            @Override
            public void onSuccess(Method method, Application response) {
                getProxy().manualReveal(ApplicationEditorPresenter.this);
                getView().editApplication(response);
            }

        });
    }

    public enum Param {

        /**
         * Parameter to indicate the app.  Passing this and specifying application ID or name will
         * allow you t
         */
        application_id;

    }
}
