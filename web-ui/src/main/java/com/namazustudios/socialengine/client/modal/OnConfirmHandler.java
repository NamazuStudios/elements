package com.namazustudios.socialengine.client.modal;

/**
 * Created by patricktwohig on 6/5/15.
 */
public interface OnConfirmHandler extends OnDismissHandler {

    /**
     * Called when the user click "YES" to confirm.
     */
    void onConfirm();

}
