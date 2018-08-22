package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.client.ui.constants.ValidationState;

import javax.validation.Validator;
import java.util.function.Consumer;

public class PrizeEditorModal extends Modal implements Editor<GameOnAddPrizeListRequest.Prize> {

    public interface Binder extends UiBinder<ModalBody, PrizeEditorModal> {}

    public interface Driver extends SimpleBeanEditorDriver<GameOnAddPrizeListRequest.Prize, PrizeEditorModal> {}

    @UiField
    FormGroup titleFormGroup;

    @UiField
    @Path("title")
    TextBox titleTextBox;

    @UiField
    @Ignore
    Label titleWarningLabel;

    @UiField
    FormGroup descriptionFormGroup;

    @UiField
    @Path("description")
    TextArea descriptionTextArea;

    @UiField
    @Ignore
    Label descriptionWarningLabel;

    @UiField
    FormGroup imageUrlFormGroup;

    @UiField
    @Path("imageUrl")
    TextBox imageUrlTextBox;

    @UiField
    @Ignore
    Label imageUrlWarningLabel;

    @UiField
    FormGroup prizeInfoFormGroup;

    @UiField
    @Path("prizeInfo")
    TextBox prizeInfoTextBox;

    @UiField
    @Ignore
    Label prizeInfoWarningLabel;

    @UiField
    Button submit;

    private PendingEdit pendingEdit;

    public PrizeEditorModal() {

        final PrizeEditorModal.Binder binder = GWT.create(PrizeEditorModal.Binder.class);
        setDataBackdrop(ModalBackdrop.FALSE);

        final ModalHeader header = new ModalHeader();
        header.setTitle("Create Prize");

        add(header);
        add(binder.createAndBindUi(this));

        reset();
        lockOut();

    }

    public void reset() {

        titleWarningLabel.setVisible(false);
        descriptionWarningLabel.setVisible(false);
        imageUrlWarningLabel.setVisible(false);
        prizeInfoWarningLabel.setVisible(false);

        titleFormGroup.setValidationState(ValidationState.NONE);
        descriptionFormGroup.setValidationState(ValidationState.NONE);
        imageUrlFormGroup.setValidationState(ValidationState.NONE);
        prizeInfoFormGroup.setValidationState(ValidationState.NONE);


        titleTextBox.setText(null);
        descriptionTextArea.setText(null);
        imageUrlTextBox.setText(null);
        prizeInfoTextBox.setText(null);

    }

    public void lockOut() {
        submit.setEnabled(false);
        titleTextBox.setEnabled(false);
        descriptionTextArea.setEnabled(false);
        imageUrlTextBox.setEnabled(false);
        prizeInfoTextBox.setEnabled(false);
    }

    public void unlock() {
        submit.setEnabled(true);
        titleTextBox.setEnabled(true);
        descriptionTextArea.setEnabled(true);
        imageUrlTextBox.setEnabled(true);
        prizeInfoTextBox.setEnabled(true);
    }

    public void createNewPrize(final Validator validator,
                               final Consumer<GameOnAddPrizeListRequest.Prize> success,
                               final Consumer<GameOnAddPrizeListRequest.Prize> failure) {
        pendingEdit = new PendingEdit(validator, success, failure);
        reset();
        unlock();
    }

    @UiHandler("submit")
    public void submit(final ClickEvent ev) {
        if (pendingEdit != null) pendingEdit.validateAndFinish();
    }

    private class PendingEdit {

        private final Validator validator;

        private final Consumer<GameOnAddPrizeListRequest.Prize> success;

        private final Consumer<GameOnAddPrizeListRequest.Prize> failure;

        private final Driver driver = GWT.create(Driver.class);

        public PendingEdit(
                final Validator validator,
                final Consumer<GameOnAddPrizeListRequest.Prize> success,
                final Consumer<GameOnAddPrizeListRequest.Prize> failure) {

            this.validator = validator;
            this.success = success;
            this.failure = failure;

            driver.initialize(PrizeEditorModal.this);
            driver.edit(new GameOnAddPrizeListRequest.Prize());

        }

        public void validateAndFinish() {

            final GameOnAddPrizeListRequest.Prize prize = driver.flush();

            boolean failed = false;

            if (validator.validateProperty(prize, "title").isEmpty()) {
                titleFormGroup.setValidationState(ValidationState.NONE);
                titleWarningLabel.setVisible(true);
            } else {
                failed = true;
                titleFormGroup.setValidationState(ValidationState.ERROR);
                titleWarningLabel.setVisible(false);
            }

            if (validator.validateProperty(prize, "description").isEmpty()) {
                descriptionFormGroup.setValidationState(ValidationState.NONE);
                descriptionWarningLabel.setVisible(false);
            } else {
                failed = true;
                descriptionFormGroup.setValidationState(ValidationState.ERROR);
                descriptionWarningLabel.setVisible(true);
            }

            if (validator.validateProperty(prize, "imageUrl").isEmpty()) {
                imageUrlFormGroup.setValidationState(ValidationState.NONE);
                imageUrlWarningLabel.setVisible(false);
            } else {
                failed = true;
                imageUrlFormGroup.setValidationState(ValidationState.ERROR);
                imageUrlWarningLabel.setVisible(true);
            }

            if (validator.validateProperty(prize, "prizeInfo").isEmpty()) {
                prizeInfoFormGroup.setValidationState(ValidationState.NONE);
                prizeInfoWarningLabel.setVisible(false);
            } else {
                failed = true;
                prizeInfoFormGroup.setValidationState(ValidationState.ERROR);
                prizeInfoWarningLabel.setVisible(true);
            }

            if (failed) {
                failure.accept(prize);
            } else {
                success.accept(prize);
            }

        }

    }

}
