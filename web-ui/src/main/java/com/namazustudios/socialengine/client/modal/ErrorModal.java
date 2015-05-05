package com.namazustudios.socialengine.client.modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Label;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;

/**
 * Created by patricktwohig on 4/30/15.
 */
public class ErrorModal extends Modal {

    private final Label errorTextLabel;

    private OnDismissHandler onDismissHandler;

    public ErrorModal() {

        setFade(true);
        setClosable(true);
        setTitle("Ooops...something went wrong.");

        final ModalBody modalBody = new ModalBody();
        add(modalBody);

        errorTextLabel = new Label();
        modalBody.add(errorTextLabel);

        final ModalFooter modalFooter = new ModalFooter();
        add(modalFooter);

        final Button button = new Button();
        button.setText("Dismiss");
        modalFooter.add(button);

        button.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }

        });

    }

    public Label getErrorTextLabel() {
        return errorTextLabel;
    }

    public void setErrorMessage(final String errorMessage) {
        getErrorTextLabel().setText(errorMessage);
    }

    public void setMessageWithThrowable(final Throwable th) {
        getErrorTextLabel().setText(th.getLocalizedMessage());
    }

    public void setOnDismissHandler(OnDismissHandler onDismissHandler) {
        this.onDismissHandler = onDismissHandler;
    }

    public void show(final OnDismissHandler onDismissHandler) {
        setOnDismissHandler(onDismissHandler);
        show();
    }

    @Override
    protected void onHide(Event evt) {
        super.onHide(evt);
    }

    @Override
    protected void onShow(Event evt) {
        super.onShow(evt);
        if (onDismissHandler != null) {
            onDismissHandler.onDismiss();
        }
    }

}
