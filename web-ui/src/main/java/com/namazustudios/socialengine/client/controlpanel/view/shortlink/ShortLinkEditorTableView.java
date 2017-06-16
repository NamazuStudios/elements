package com.namazustudios.socialengine.client.controlpanel.view.shortlink;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
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
import org.gwtbootstrap3.extras.notify.client.ui.Notify;

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

        destinationColumn.setFieldUpdater((index, object, value) -> Window.open(object.getDestinationURL(), "_blank", ""));

        final Column<ShortLink, String> shortLinkColumn = new Column<ShortLink, String>(new ClickableTextCell()) {
            @Override
            public String getValue(ShortLink object) {
                return object.getShortLinkURL();
            }
        };

        shortLinkColumn.setFieldUpdater((index, object, value) -> Window.open(object.getShortLinkURL(), "_blank", ""));

        final Column<ShortLink, String> deleteColumn = new Column<ShortLink,String>(new ButtonCell()) {
            @Override
            public String getValue(ShortLink object) {
                return "Delete";
            }
        };

        deleteColumn.setFieldUpdater((index, object, value) -> confirmDelete(object));

        shortLinkEditorCellTable.addColumn(destinationColumn, "Link Destination");
        shortLinkEditorCellTable.addColumn(shortLinkColumn, "Short Link");
        shortLinkEditorCellTable.addColumn(deleteColumn);

        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.INFO);
        emptyLabel.setText("No short links found matching query.");
        shortLinkEditorCellTable.setEmptyTableWidget(emptyLabel);

        shortLinkEditorCellTable.addRangeChangeHandler(event -> shortLinkEditorCellTablePagination.rebuild(simplePager));
        asyncUserDataProvider.addRefreshListener(() -> shortLinkEditorCellTablePagination.rebuild(simplePager));

        simplePager.setDisplay(shortLinkEditorCellTable);
        shortLinkEditorCellTablePagination.clear();
        asyncUserDataProvider.addDataDisplay(shortLinkEditorCellTable);

        setupSearch();

    }

    private void setupSearch() {
        searchLinksSearchTextBox.addChangeHandler(event -> {
            asyncUserDataProvider.setSearchFilter(searchLinksSearchTextBox.getText());
            shortLinkEditorCellTable.setVisibleRangeAndClearData(shortLinkEditorCellTable.getVisibleRange(), true);
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
                Notify.notify(shortLink.getShortLinkURL() + " deleted.");
                shortLinkEditorCellTable.setVisibleRangeAndClearData(shortLinkEditorCellTable.getVisibleRange(), true);
            }

        });
    }

    private void showErrorModal(final Throwable throwable) {
        errorModal.setMessageWithThrowable(throwable);
        errorModal.show();
    }

}
