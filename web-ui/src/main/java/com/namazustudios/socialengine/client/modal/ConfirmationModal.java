package com.namazustudios.socialengine.client.modal;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;

/**
 * Created by patricktwohig on 6/5/15.
 */
public class ConfirmationModal extends Modal {

    private final Label errorTextLabel;

    private OnConfirmHandler onConfirmHandler;

    public ConfirmationModal() {

        setFade(true);
        setClosable(true);
        setTitle("Confirm?");

        final ModalBody modalBody = new ModalBody();
        add(modalBody);

        errorTextLabel = new Label();
        modalBody.add(errorTextLabel);

        final ModalFooter modalFooter = new ModalFooter();
        add(modalFooter);

        final Button confirmButton = new Button();
        confirmButton.setText("Confirm");
        modalFooter.add(confirmButton);

        confirmButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                hide();

                if (onConfirmHandler != null) {
                    onConfirmHandler.onConfirm();
                }

            }

        });

        final Button dismissButton = new Button();
        dismissButton.setText("Dismiss");
        modalFooter.add(dismissButton);

        dismissButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {

                hide();

                if (onConfirmHandler != null) {
                    onConfirmHandler.onDismiss();
                }

            }

        });

    }

    public Label getErrorTextLabel() {
        return errorTextLabel;
    }


    public OnConfirmHandler getOnConfirmHandler() {
        return onConfirmHandler;
    }

    public void setOnConfirmHandler(OnConfirmHandler onConfirmHandler) {
        this.onConfirmHandler = onConfirmHandler;
    }

}
