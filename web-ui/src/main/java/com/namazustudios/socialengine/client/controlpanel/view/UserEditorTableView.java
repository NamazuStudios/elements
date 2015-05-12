package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.RangeChangeEvent;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.model.User;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 5/11/15.
 */
public class UserEditorTableView extends ViewImpl implements UserEditorTablePresenter.MyView {

    interface UserEditorTableViewUiBinder extends UiBinder<Panel, UserEditorTableView> {}

    @UiField
    CellTable<User> userEditorCellTable;

    @UiField
    Pagination userEditorCellTablePagination;

    @UiField
    TextBox searchUsersTextBox;

    private final UserDataProvider asyncUserDataProvider;

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public UserEditorTableView(
            final UserEditorTableViewUiBinder userEditorTableViewUiBinder,
            final UserDataProvider asyncUserDataProvider) {

        initWidget(userEditorTableViewUiBinder.createAndBindUi(this));

        this.asyncUserDataProvider = asyncUserDataProvider;

        final Column<User, String> nameColumn;

        nameColumn = new Column<User, String>(new EditTextCell()) {
            @Override
            public String getValue(User object) {
                return object.getName();
            }
        };

        nameColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            @Override
            public void update(int index, User object, String value) {
                object.setName(value);
            }
        });

        final Column<User, String> emailColumn;

        emailColumn = new Column<User, String>(new EditTextCell()) {
            @Override
            public String getValue(User object) {
                return object.getEmail();
            }
        };

        emailColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            @Override
            public void update(int index, User object, String value) {
                object.setEmail(value);
            }
        });

        final Column<User, String> levelColumn;

        levelColumn = new Column<User, String>(new EditTextCell()) {
            @Override
            public String getValue(User object) {

                if (object.getLevel() == null) {
                    return "";
                }

                switch (object.getLevel()) {
                    case UNPRIVILEGED: return "Unprivileged User";
                    case USER:         return "Normal User";
                    case SUPERUSER:    return "Super-User";
                    default:           return "";
                }

            }
        };

        final Column<User, String> editColumn;

        editColumn = new Column<User, String>(new ButtonCell()) {
            @Override
            public String getValue(User object) {
                return "Edit...";
            }
        };

        editColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            @Override
            public void update(int index, User object, String value) {
                //TODO User PlaceManager to edit the user
                Window.alert("Editing user " + object.getName());
            }
        });

        final Column<User, String> deleteColumn;

        deleteColumn = new Column<User, String>(new ButtonCell()) {
            @Override
            public String getValue(User object) {
                return "Delete";
            }
        };

        deleteColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            @Override
            public void update(int index, User object, String value) {
                //TODO Confirm and make REST call to delete user
                Window.alert("Deleting using " + object.getName());
            }
        });

        userEditorCellTable.addColumn(nameColumn, "User Name");
        userEditorCellTable.addColumn(emailColumn, "Email Address");
        userEditorCellTable.addColumn(levelColumn, "User Access Level");
        userEditorCellTable.addColumn(editColumn);
        userEditorCellTable.addColumn(deleteColumn);

        userEditorCellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                userEditorCellTablePagination.rebuild(simplePager);
            }
        });

        asyncUserDataProvider.addRefreshListener(new UserDataProvider.AsyncRefreshListener() {
            @Override
            public void onRefresh() {
                userEditorCellTablePagination.rebuild(simplePager);
            }
        });

        simplePager.setDisplay(userEditorCellTable);
        userEditorCellTablePagination.clear();
        asyncUserDataProvider.addDataDisplay(userEditorCellTable);


    }



}
