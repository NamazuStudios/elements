package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.base.Strings;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/5/15.
 */
public class UserEditorPresenter extends Presenter<UserEditorPresenter.MyView, UserEditorPresenter.MyProxy> {

    @ProxyCodeSplit
    @NameToken(NameTokens.USER_EDIT)
    public interface MyProxy extends ProxyPlace<UserEditorPresenter> {}

    public interface MyView extends View {

        void reset();

        void createUser();

        void editUser(final User user);

    }

    @Inject
    private UserClient userClient;

    @Inject
    public UserEditorPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
        super(eventBus, view, proxy, ControlPanelPresenter.SET_MAIN_CONTENT_TYPE);
    }

    @Override
    public boolean useManualReveal() {
        return true;
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {

        super.prepareFromRequest(request);

        final String userName = request.getParameter(Param.user.name().toLowerCase(), "").trim();

        if (Strings.isNullOrEmpty(userName)) {
            getView().createUser();
            getProxy().manualReveal(this);
        } else {
            loadUserAndShow(userName);
        }

    }

    private void loadUserAndShow(final String userName) {
        userClient.getUser(userName, new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                getProxy().manualReveal(UserEditorPresenter.this);
                getView().createUser();
                Notify.notify("Could not load user " + method.getData());
            }

            @Override
            public void onSuccess(Method method, User user) {
                getProxy().manualReveal(UserEditorPresenter.this);
                getView().editUser(user);
            }

        });
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        getView().reset();
    }

    @Override
    protected void onReset() {
        super.onReset();
        getView().reset();
    }

    public enum Param {

        /**
         * Pass this param with a user ID to put the {@link UserEditorView} into editing mode
         * which will allow the user to edit an account.
         */
        user

    }

}
