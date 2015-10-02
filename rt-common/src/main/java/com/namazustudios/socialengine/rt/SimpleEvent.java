package com.namazustudios.socialengine.rt;

/**
 * Created by patricktwohig on 8/8/15.
 */
public class SimpleEvent implements Event {

    private SimpleEventHeader eventHeader;

    private Object payload;

    @Override
    public SimpleEventHeader getEventHeader() {
        return eventHeader;
    }

    public void setEventHeader(final SimpleEventHeader eventHeader) {
        this.eventHeader = eventHeader;
    }

    @Override
    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final SimpleEvent simpleEvent = new SimpleEvent();

        private final SimpleEventHeader simpleEventHeader = new SimpleEventHeader();

        public Builder() {
            simpleEvent.setEventHeader(simpleEventHeader);
        }

        public Builder payload(final Object payload) {
            simpleEvent.setPayload(payload);
            return this;
        }

        public Builder header(final EventHeader eventHeader) {

            if (eventHeader != null) {
                simpleEventHeader.setName(eventHeader.getName());
                simpleEventHeader.setPath(eventHeader.getPath());
            }

            return this;
        }

        public Builder event(final Event event) {

            if (event.getEventHeader() != null) {
                header(event.getEventHeader());
            }

            if (event.getPayload() != null) {
                payload(event.getPayload());
            }

            return this;

        }

        public Builder name(final String name) {
            simpleEventHeader.setName(name);
            return this;
        }

        public Builder path(final String path) {
            simpleEventHeader.setPath(path);
            return this;
        }

        public Builder path(final Path path) {
            return path(path.toNormalizedPathString());
        }

        public SimpleEvent build() {
            return simpleEvent;
        }

    }

}
