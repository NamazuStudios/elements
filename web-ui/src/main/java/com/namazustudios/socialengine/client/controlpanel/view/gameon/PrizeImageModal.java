package com.namazustudios.socialengine.client.controlpanel.view.gameon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.namazustudios.socialengine.model.gameon.admin.GameOnGetPrizeListResponse;
import org.gwtbootstrap3.client.ui.ImageAnchor;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;

import static java.lang.String.format;

public class PrizeImageModal extends Modal {

    public interface Binder extends UiBinder<ModalBody, PrizeImageModal> {}

    @UiField
    ImageAnchor imageAnchor;

    private final ModalHeader header;

    private GameOnGetPrizeListResponse.Prize prize;

    public PrizeImageModal() {

        final PrizeImageModal.Binder binder = GWT.create(PrizeImageModal.Binder.class);
        setDataBackdrop(ModalBackdrop.FALSE);

        header = new ModalHeader();
        header.setTitle("Image Preview");

        add(header);
        add(binder.createAndBindUi(this));

    }

    public GameOnGetPrizeListResponse.Prize getPrize() {
        return prize;
    }

    public void setPrize(GameOnGetPrizeListResponse.Prize prize) {

        this.prize = prize;

        if (prize == null) {
            header.setTitle("Image Preivew");
            imageAnchor.setUrl(null);
        } else {
            final String title = "Image Preview - " + prize.getTitle();
            final String imageUrl = prize.getImageUrl();
            header.setTitle(title);
            imageAnchor.setUrl(imageUrl);
        }

    }

}
