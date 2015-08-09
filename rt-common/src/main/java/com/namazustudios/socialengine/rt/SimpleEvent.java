package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/8/15.
 */
public class SimpleEvent implements Event {

    private EventHeader eventHeader;

    private Object payload;

    @Override
    public EventHeader getEventHeader() {
        return eventHeader;
    }

    public void setEventHeader(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

}
