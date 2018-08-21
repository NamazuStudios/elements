package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Pagination;
import org.gwtbootstrap3.client.ui.constants.LabelType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import javax.inject.Inject;
import java.util.Date;

import static com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat.DATE_TIME_MEDIUM;
import static org.gwtbootstrap3.client.ui.constants.ButtonType.PRIMARY;
import static org.gwtbootstrap3.client.ui.constants.IconType.PICTURE_O;

public class PrizeEditorTableView extends ViewImpl implements PrizeEditorTablePresenter.MyView {

    public static final int PAGE_SIZE = 20;

    public interface PrizeEditorTableViewBinder extends UiBinder<Container, PrizeEditorTableView> {}

    @UiField
    Button addPrize;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @UiField
    CellTable<GameOnGetPrizeListResponse.Prize> prizeEditorCellTable;

    @UiField
    Pagination prizeEditorCellTablePagination;

    @UiField
    PrizeImageModal prizeImageModal;

    @UiField
    PrizeEditorModal editPrizeModal;

    private final PrizeDataProvider prizeDataProvider;

    private final SimplePager simplePager = new SimplePager();

    @Inject
    public PrizeEditorTableView(final PrizeDataProvider prizeDataProvider,
                                final PrizeEditorTableViewBinder prizeEditorTableViewBinder) {

        this.prizeDataProvider = prizeDataProvider;
        initWidget(prizeEditorTableViewBinder.createAndBindUi(this));

        final Column<GameOnGetPrizeListResponse.Prize, String> prizeIdColumn =
            new Column<GameOnGetPrizeListResponse.Prize, String>(new TextCell()) {

                @Override
                public String getValue(GameOnGetPrizeListResponse.Prize object) {
                    return object.getPrizeId();
                }

            };

        final Column<GameOnGetPrizeListResponse.Prize, String> titleColumn =
            new Column<GameOnGetPrizeListResponse.Prize, String>(new TextCell()) {
                @Override
                public String getValue(final GameOnGetPrizeListResponse.Prize object) {
                    return object.getTitle();
                }
            };

        final Column<GameOnGetPrizeListResponse.Prize, String> descriptionColumn =
            new Column<GameOnGetPrizeListResponse.Prize, String>(new TextCell()) {
                @Override
                public String getValue(final GameOnGetPrizeListResponse.Prize object) {
                    return object.getDescription();
                }
            };

        final Column<GameOnGetPrizeListResponse.Prize, String> prizeInfoTypeColumn =
            new Column<GameOnGetPrizeListResponse.Prize, String>(new TextCell()) {
                @Override
                public String getValue(final GameOnGetPrizeListResponse.Prize object) {
                    return object.getPrizeInfoType().name();
                }
            };

        final Column<GameOnGetPrizeListResponse.Prize, String> expirationColumn =
            new Column<GameOnGetPrizeListResponse.Prize, String>(new TextCell()) {

                private final DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DATE_TIME_MEDIUM);

                @Override
                public String getValue(final GameOnGetPrizeListResponse.Prize object) {
                    final Long expiration = object.getDateOfExpiration();
                    return expiration == null ? "Never" : formatDate(expiration);
                }

                private String formatDate(final Long expiration) {
                    final Date date = new Date(expiration);
                    return dateTimeFormat.format(date);
                }

            };

        final Column<GameOnGetPrizeListResponse.Prize, String> imageUrlColumn =
            new Column<GameOnGetPrizeListResponse.Prize, String>(new ButtonCell(PRIMARY, PICTURE_O)) {
                @Override
                public String getValue(GameOnGetPrizeListResponse.Prize object) {
                    final String url = object.getImageUrl();
                    return url == null || url.trim().isEmpty() ? "Not Available" : "Preview ";
                }
            };

        imageUrlColumn.setFieldUpdater((index, object, value) -> {
            if (object.getImageUrl() != null && !object.getImageUrl().isEmpty()) {
                prizeImageModal.setPrize(object);
                prizeImageModal.show();
            }
        });

        // Empty label
        final Label emptyLabel = new Label();
        emptyLabel.setType(LabelType.INFO);
        emptyLabel.setText("No Prizes Defined.");
        prizeEditorCellTable.setEmptyTableWidget(emptyLabel);

        // Columns
        prizeEditorCellTable.addColumn(prizeIdColumn, "Prize ID");
        prizeEditorCellTable.addColumn(titleColumn, "Title");
        prizeEditorCellTable.addColumn(descriptionColumn, "Description");
        prizeEditorCellTable.addColumn(prizeInfoTypeColumn, "Prize Info Type");
        prizeEditorCellTable.addColumn(expirationColumn, "Expires At");
        prizeEditorCellTable.addColumn(imageUrlColumn, "Image");

        // Pagination and events

        prizeEditorCellTable.addRangeChangeHandler(event -> prizeEditorCellTablePagination.rebuild(simplePager));
        prizeDataProvider.addPrizesLoadedListener((method, re) -> prizeEditorCellTablePagination.rebuild(simplePager));
        prizeDataProvider.addPrizesFailedListener((method, ex) -> prizeEditorCellTablePagination.rebuild(simplePager));

        simplePager.setDisplay(prizeEditorCellTable);
        simplePager.setPageSize(PAGE_SIZE);
        prizeEditorCellTablePagination.clear();

        prizeDataProvider.addDataDisplay(prizeEditorCellTable);

    }

    @Override
    public void reset() {
        prizeDataProvider.clear();
    }

    @Override
    public void load(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {
        prizeDataProvider.reconfigure(gameOnApplicationConfiguration);
    }

    @UiHandler("addPrize")
    public void addPrize(final ClickEvent clickEvent) {
        editPrizeModal.show();
    }

}
