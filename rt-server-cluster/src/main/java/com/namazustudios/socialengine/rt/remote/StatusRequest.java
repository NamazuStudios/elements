package com.namazustudios.socialengine.rt.remote;

import javolution.io.Struct;

public class StatusRequest extends Struct {
    public static StatusRequest buildStatusRequest() {
        final StatusRequest statusRequest = new StatusRequest();
        return statusRequest;
    }
}
