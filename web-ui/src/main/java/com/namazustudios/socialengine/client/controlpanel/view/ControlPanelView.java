package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.rest.service.LoginService;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/1/15.
 */
public class ControlPanelView extends ViewImpl implements ControlPanelPresenter.MyView {

    interface ControlPanelViewUiBinder extends UiBinder<ScrollPanel, ControlPanelView> {}

    @UiField
    SimplePanel contentContainer;

    @Inject
    private LoginService loginService;

    @Inject
    private PlaceManager placeManager;

    @Inject
    public ControlPanelView(ControlPanelViewUiBinder controlPanelViewUiBinder) {
        initWidget(controlPanelViewUiBinder.createAndBindUi(this));
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
        if (slot == ControlPanelPresenter.SET_MAIN_CONTENT_TYPE) {
            contentContainer.setWidget(content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    @UiHandler("logout")
    void onClickLogout(final ClickEvent clickEvent) {
        loginService.logout(new MethodCallback<Void>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                revealLoginPlace();
            }

            @Override
            public void onSuccess(Method method, Void aVoid) {
                revealLoginPlace();
            }

            private void revealLoginPlace() {

                final PlaceRequest placeRequest = new PlaceRequest.Builder()
                        .nameToken(NameTokens.LOGIN)
                        .with(LoginViewPresenter.Param.refresh.name(), Boolean.FALSE.toString())
                        .build();

                placeManager.revealPlace(placeRequest);

            }

        });
    }

}
