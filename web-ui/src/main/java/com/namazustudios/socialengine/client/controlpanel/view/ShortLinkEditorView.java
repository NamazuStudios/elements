package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.rest.client.ShortLinkClient;
import com.namazustudios.socialengine.model.ShortLink;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

import javax.inject.Inject;
import javax.validation.Validator;

/**
 * Allows for the creation of a short link.
 *
 * Created by patricktwohig on 6/12/15.
 */
public class ShortLinkEditorView extends ViewImpl implements ShortLinkEditorPresenter.MyView, Editor<ShortLink> {

    interface Driver extends SimpleBeanEditorDriver<ShortLink, ShortLinkEditorView> {}

    interface ShortLinkEditorUiBinder extends UiBinder<Panel, ShortLinkEditorView> {}

    @UiField
    @Ignore
    ErrorModal errorModal;

    @UiField
    @Ignore
    FormGroup destinationFormGroup;

    @UiField
    @Path("destinationURL")
    TextBox destinationTextBox;

    @UiField
    @Ignore
    Label destinationWarningLabel;

    @UiField
    Button create;

    @Inject
    private Driver driver;

    @Inject
    private Validator validator;

    @Inject
    private ShortLinkClient shortLinkClient;

    @Inject
    private PlaceManager placeManager;

    @Inject
    public ShortLinkEditorView(final ShortLinkEditorUiBinder shortLinkEditorUiBinder){
        initWidget(shortLinkEditorUiBinder.createAndBindUi(this));
    }

    @Override
    protected void onAttach() {

        reset();

        driver.initialize(this);
        driver.edit(new ShortLink());

    }

    private void lockOut() {
        create.setEnabled(false);
        destinationTextBox.setEnabled(false);
    }

    private void unlock() {
        create.setEnabled(true);
        destinationTextBox.setEnabled(true);
    }

    private void reset() {
        destinationWarningLabel.setVisible(false);
        destinationFormGroup.setValidationState(ValidationState.NONE);
    }

    @UiHandler("create")
    void onClickCreate(final ClickEvent ev) {

        final ShortLink shortLink = driver.flush();

        boolean failed;

        if (validator.validateProperty(shortLink, "destinationURL").isEmpty()) {
            failed = false;
            destinationWarningLabel.setVisible(false);
            destinationFormGroup.setValidationState(ValidationState.NONE);
        } else {
            failed = true;
            destinationWarningLabel.setVisible(true);
            destinationFormGroup.setValidationState(ValidationState.ERROR);
        }

        if (!failed) {
            submit(shortLink);
        }

    }

    private void submit(final ShortLink shortLink) {

        lockOut();

        shortLinkClient.createNewShortLink(shortLink, new MethodCallback<ShortLink>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                unlock();
                errorModal.setMessageWithThrowable(throwable);
                errorModal.show();
            }

            @Override
            public void onSuccess(Method method, ShortLink shortLink) {

                unlock();
                Notify.notify("Successfully created short link.");

                final PlaceRequest placeRequest = new PlaceRequest.Builder()
                        .nameToken(NameTokens.MAIN)
                        .build();

                placeManager.revealPlace(placeRequest);

            }

        });
    }

}
