package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.gwtplatform.mvp.client.ViewImpl;
import com.namazustudios.socialengine.client.modal.ConfirmationModal;
import com.namazustudios.socialengine.client.modal.ErrorModal;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.GameOnApplicationConfiguration;
import com.namazustudios.socialengine.model.gameon.admin.GetPrizeListResponse;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import javax.inject.Inject;

public class PrizeEditorTableView extends ViewImpl implements PrizeEditorTablePresenter.MyView {

    public interface PrizeEditorTableViewBinder extends UiBinder<Panel, PrizeEditorTableView> {}

    @UiField
    Button addPrize;

    @UiField
    ErrorModal errorModal;

    @UiField
    ConfirmationModal confirmationModal;

    @UiField
    CellTable<GetPrizeListResponse.Prize> prizeEditorCellTable;

    @Inject
    public PrizeEditorTableView(final PrizeDataProvider prizeDataProvider,
                                final PrizeEditorTableViewBinder prizeEditorTableViewBinder) {

    }

    @Override
    public void reset() {

    }

    @Override
    public void load(final GameOnApplicationConfiguration gameOnApplicationConfiguration) {

    }

}
