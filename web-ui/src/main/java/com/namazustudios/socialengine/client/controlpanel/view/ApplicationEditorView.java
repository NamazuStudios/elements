package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import org.gwtbootstrap3.client.ui.*;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import javax.inject.Inject;

import java.util.function.Consumer;

import static org.gwtbootstrap3.client.ui.constants.ValidationState.NONE;

/**
 * Created by patricktwohig on 6/1/17.
 */
public class ApplicationEditorView extends ViewImpl implements ApplicationEditorPresenter.MyView, Editor<Application> {

    interface Driver extends SimpleBeanEditorDriver<Application, ApplicationEditorView> {};

    interface ApplicationEditorViewBinder extends UiBinder<Container, ApplicationEditorView> {}

    @UiField
    ErrorModal errorModal;

    @UiField
    FormGroup applicationNameFormGroup;

    @UiField
    @Path("name")
    TextBox applicationNameTextBox;

    @UiField
    Label applicationNameWarningLabel;

    @UiField
    FormGroup applicationDescriptionFormGroup;

    @UiField
    @Path("name")
    TextBox applicationDescriptionTextBox;

    @UiField
    Label applicationDescriptionWarningLabel;

    @UiField
    CellTable<ApplicationProfile> applicationProfileCellTable;

    @UiField
    Pagination applicationProfileCellTablePagination;

    @Inject
    private Driver driver;

    private Consumer<Application> save = a -> { lockOut(); createNewUser(); };

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public ApplicationEditorView(
            final ApplicationEditorViewBinder applicationEditorViewBinder) {
        initWidget(applicationEditorViewBinder.createAndBindUi(this));
    }

    public void lockOut() {
        applicationNameTextBox.setEnabled(false);
        applicationDescriptionTextBox.setEnabled(false);
    }

    public void unlock() {
        applicationNameTextBox.setEnabled(true);
        applicationDescriptionTextBox.setEnabled(true);
    }

    @Override
    public void reset() {

        applicationNameTextBox.setEnabled(true);
        applicationDescriptionTextBox.setEnabled(true);

        applicationNameTextBox.setText("");
        applicationDescriptionTextBox.setText("");
        applicationNameFormGroup.setValidationState(NONE);
        applicationDescriptionFormGroup.setValidationState(NONE);

        applicationNameWarningLabel.setVisible(false);
        applicationDescriptionWarningLabel.setVisible(false);

        // TODO: Figure out how to clear the table

    }

    @Override
    public void createApplication() {

        reset();

        driver.initialize(this);
        driver.edit(new Application());

        save = a -> {
            lockOut();
            createNewUser();
        };

    }

    @Override
    public void editApplication(Application application) {
        reset();

        driver.initialize(this);
        driver.edit(application);

        save = a -> {
            lockOut();
            updateExistingApplication(a);
        };
    }

    private void createNewUser() {

    }

    private void updateExistingApplication(final Application application) {

    }

}
