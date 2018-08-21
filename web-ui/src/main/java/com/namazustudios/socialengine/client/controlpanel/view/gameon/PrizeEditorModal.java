package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.namazustudios.socialengine.model.gameon.admin.GameOnAddPrizeListRequest;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;

public class PrizeEditorModal extends Modal implements Editor<GameOnAddPrizeListRequest.Prize> {

    public interface Binder extends UiBinder<ModalBody, PrizeEditorModal> {}

    public PrizeEditorModal() {

        final PrizeEditorModal.Binder binder = GWT.create(PrizeEditorModal.Binder.class);
        setDataBackdrop(ModalBackdrop.FALSE);

        final ModalHeader header = new ModalHeader();
        header.setTitle("Create Prize");

        add(header);
        add(binder.createAndBindUi(this));

    }

}
