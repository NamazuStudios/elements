package com.namazustudios.socialengine.rt;

import javax.inject.Inject;

public class SimpleEventContext implements EventContext {

    private EventService eventService;

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void postAsync(String eventName, Attributes attributes, Object... args) {
        getEventService().postAsync(eventName, attributes, args);
    }

    public EventService getEventService() {
        return eventService;
    }

    @Inject
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }
}
