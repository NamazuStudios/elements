package com.namazustudios.socialengine.client.controlpanel.view;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.RangeChangeEvent;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.client.modal.OnConfirmHandler;
import com.namazustudios.socialengine.client.rest.client.ShortLinkClient;
import com.namazustudios.socialengine.model.ShortLink;
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

/**
 * Allows for the editing/display of all short links in the system.
 *
 * Created by patricktwohig on 6/24/15.
 */
public class ShortLinkEditorTableView extends ViewImpl implements ShortLinkEditorTablePresenter.MyView {

    interface ShortLinkEditorTableViewUiBinder extends UiBinder<Panel, ShortLinkEditorTableView> {}

    @UiField
    CellTable<ShortLink> shortLinkEditorCellTable;

    @UiField
    Pagination shortLinkEditorCellTablePagination;

    @UiField
    TextBox searchLinksSearchTextBox;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @Inject
    private ShortLinkClient shortLinkClient;

    private final ShortLinkDataProvider asyncUserDataProvider;

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public ShortLinkEditorTableView(
            ShortLinkEditorTableViewUiBinder shortLinkEditorTableViewUiBinder,
            ShortLinkDataProvider asyncUserDataProvider) {
        initWidget(shortLinkEditorTableViewUiBinder.createAndBindUi(this));

        this.asyncUserDataProvider = asyncUserDataProvider;

        final Column<ShortLink, String> destinationColumn = new Column<ShortLink, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ShortLink object) {
                return object.getDestinationURL();
            }
        };

        destinationColumn.setFieldUpdater(new FieldUpdater<ShortLink, String>() {
            @Override
            public void update(int index, ShortLink object, String value) {
                Window.open(object.getDestinationURL(), "_blank", "");
            }
        });

        final Column<ShortLink, String> shortLinkColumn = new Column<ShortLink, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ShortLink object) {
                return object.getShortLinkURL();
            }
        };

        shortLinkColumn.setFieldUpdater(new FieldUpdater<ShortLink, String>() {
            @Override
            public void update(int index, ShortLink object, String value) {
                Window.open(object.getShortLinkURL(), "_blank", "");
            }
        });

        final Column<ShortLink, String> deleteColumn = new Column<ShortLink,String>(new ButtonCell()) {
            @Override
            public String getValue(ShortLink object) {
                return "Delete";
            }
        };

        deleteColumn.setFieldUpdater(new FieldUpdater<ShortLink, String>() {
            @Override
            public void update(int index, ShortLink object, String value) {
                confirmDelete(object);
            }
        });

        shortLinkEditorCellTable.addColumn(destinationColumn, "Link Destination");
        shortLinkEditorCellTable.addColumn(shortLinkColumn, "Short Link");
        shortLinkEditorCellTable.addColumn(deleteColumn);

        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.DANGER);
        emptyLabel.setText("No users found matching query.");
        shortLinkEditorCellTable.setEmptyTableWidget(emptyLabel);

        shortLinkEditorCellTable.addRangeChangeHandler(new RangeChangeEvent.Handler() {
            @Override
            public void onRangeChange(RangeChangeEvent event) {
                shortLinkEditorCellTablePagination.rebuild(simplePager);
            }
        });

        asyncUserDataProvider.addRefreshListener(new UserSearchableDataProvider.AsyncRefreshListener() {
            @Override
            public void onRefresh() {
                shortLinkEditorCellTablePagination.rebuild(simplePager);
            }
        });

        simplePager.setDisplay(shortLinkEditorCellTable);
        shortLinkEditorCellTablePagination.clear();
        asyncUserDataProvider.addDataDisplay(shortLinkEditorCellTable);

        setupSearch();


    }

    private void setupSearch() {
        searchLinksSearchTextBox.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                asyncUserDataProvider.setSearchFilter(searchLinksSearchTextBox.getText());
                shortLinkEditorCellTable.setVisibleRangeAndClearData(shortLinkEditorCellTable.getVisibleRange(), true);
            }

        });
    }

    private void confirmDelete(final ShortLink shortLink) {

        confirmationModal.getErrorTextLabel().setText("Delete link " +
                shortLink.getShortLinkURL() + " to " +
                shortLink.getDestinationURL() + "?");
        confirmationModal.setOnConfirmHandler(new OnConfirmHandler() {

            @Override
            public void onConfirm() {
                delete(shortLink);
            }

            @Override
            public void onDismiss() {}

        });

        confirmationModal.show();

    }

    private void delete(final ShortLink shortLink) {
        shortLinkClient.delete(shortLink.getId(), new MethodCallback<Void>() {

            @Override
            public void onFailure(Method method, Throwable throwable) {
                showErrorModal(throwable);
            }

            @Override
            public void onSuccess(Method method, Void aVoid) {
                Growl.growl(shortLink.getShortLinkURL() + " deleted.");
                shortLinkEditorCellTable.setVisibleRangeAndClearData(shortLinkEditorCellTable.getVisibleRange(), true);
            }

        });
    }

    private void showErrorModal(final Throwable throwable) {
        errorModal.setMessageWithThrowable(throwable);
        errorModal.show();
    }

}
