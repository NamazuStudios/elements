package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.common.collect.ImmutableList;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.view.client.RangeChangeEvent;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.namazustudios.socialengine.client.controlpanel.NameTokens;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.modal.OnConfirmHandler;
import com.namazustudios.socialengine.client.rest.client.UserClient;
import com.namazustudios.socialengine.model.User;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.LabelType;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.growl.client.ui.Growl;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by patricktwohig on 5/11/15.
 */
public class UserEditorTableView extends ViewImpl implements UserEditorTablePresenter.MyView {

    interface UserEditorTableViewUiBinder extends UiBinder<Panel, UserEditorTableView> {}

    private static final List<String> USER_LEVEL_OPTIONS = new ImmutableList.Builder<String>()
            .add("Unprivileged User")
            .add("Normal User")
            .add("Super User")
        .build();

    @UiField
    CellTable<User> userEditorCellTable;

    @UiField
    Pagination userEditorCellTablePagination;

    @UiField
    TextBox searchUsersTextBox;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @Inject
    private UserClient userClient;

    @Inject
    private PlaceManager placeManager;

    private final UserSearchableDataProvider asyncUserDataProvider;

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public UserEditorTableView(
            final UserEditorTableViewUiBinder userEditorTableViewUiBinder,
            final UserSearchableDataProvider asyncUserDataProvider) {

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
            public void update(int index, final User object, String value) {

                final String old = object.getName();
                object.setName(value);

                save(object, index, new Runnable() {
                    @Override
                    public void run() {
                        object.setName(old);
                    }
                });

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
            public void update(int index, final User object, String value) {
                final String old = object.getEmail();
                object.setEmail(value);
                save(object, index, new Runnable() {
                    @Override
                    public void run() {
                        object.setEmail(old);
                    }
                });
            }
        });

        final Column<User, String> levelColumn;

        levelColumn = new Column<User, String>(new SelectionCell(USER_LEVEL_OPTIONS)) {
            @Override
            public String getValue(User object) {
                return (object.getLevel() == null) ? "" : USER_LEVEL_OPTIONS.get(object.getLevel().ordinal());
            }
        };

        levelColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            @Override
            public void update(int index, final User object, String value) {

                final User.Level old = object.getLevel();
                final User.Level level = User.Level.values()[USER_LEVEL_OPTIONS.indexOf(value)];

                object.setLevel(level);
                save(object, index, new Runnable() {
                    @Override
                    public void run() {
                        object.setLevel(old);
                    }
                });

            }
        });

        final Column<User, String> editColumn;

        editColumn = new Column<User, String>(new ButtonCell()) {
            @Override
            public String getValue(User object) {
                return "Edit";
            }
        };

        editColumn.setFieldUpdater(new FieldUpdater<User, String>() {
            @Override
            public void update(int index, User object, String value) {

                final PlaceRequest placeRequest = new PlaceRequest.Builder()
                        .nameToken(NameTokens.USER_EDIT)
                        .with(UserEditorPresenter.Param.user.toString(), object.getName())
                        .build();

                placeManager.revealPlace(placeRequest);

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
                confirmDelete(object);
            }

        });

        userEditorCellTable.addColumn(nameColumn, "User Name");
        userEditorCellTable.addColumn(emailColumn, "Email Address");
        userEditorCellTable.addColumn(levelColumn, "User Access Level");
        userEditorCellTable.addColumn(editColumn);
        userEditorCellTable.addColumn(deleteColumn);

        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.DANGER);
        emptyLabel.setText("No users found matching query.");
        userEditorCellTable.setEmptyTableWidget(emptyLabel);

        userEditorCellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                userEditorCellTablePagination.rebuild(simplePager);
            }
        });

        asyncUserDataProvider.addRefreshListener(new UserSearchableDataProvider.AsyncRefreshListener() {
            @Override
            public void onRefresh() {
                userEditorCellTablePagination.rebuild(simplePager);
            }
        });

        simplePager.setDisplay(userEditorCellTable);
        userEditorCellTablePagination.clear();
        asyncUserDataProvider.addDataDisplay(userEditorCellTable);

        setupSearch();

    }

    private void setupSearch() {
        searchUsersTextBox.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                asyncUserDataProvider.setSearchFilter(searchUsersTextBox.getText());
                userEditorCellTable.setVisibleRangeAndClearData(userEditorCellTable.getVisibleRange(), true);
            }

        });
    }

    private void save(final User user, final int toRedraw, final Runnable unwwind) {
        userClient.updateUser(user.getName(), null, user, new MethodCallback<User>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                unwwind.run();
                userEditorCellTable.redrawRow(toRedraw);
                showErrorModal(throwable);
            }

            @Override
            public void onSuccess(Method method, User user) {
                Growl.growl(user.getName() + " updated.");
                userEditorCellTable.redrawRow(toRedraw);
            }

        });
    }

    private void confirmDelete(final User user) {

        confirmationModal.getErrorTextLabel().setText("Delete user " + user.getName() + "?");
        confirmationModal.setOnConfirmHandler(new OnConfirmHandler() {

            @Override
            public void onConfirm() {
                delete(user);
            }

            @Override
            public void onDismiss() {}

        });

        confirmationModal.show();

    }

    private void delete(final User user) {
        userClient.deleteUser(user.getName(), new MethodCallback<Void>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                showErrorModal(throwable);
            }

            @Override
            public void onSuccess(Method method, Void aVoid) {
                Growl.growl(user.getName() + " deleted.");
                userEditorCellTable.setVisibleRangeAndClearData(userEditorCellTable.getVisibleRange(), true);
            }

        });
    }

    private void showErrorModal(final Throwable throwable) {
        errorModal.setMessageWithThrowable(throwable);
        errorModal.show();
    }

}
