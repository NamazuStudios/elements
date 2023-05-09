package dev.getelements.elements.rt;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleEventContext implements EventContext {


    private long timeout;

    private EventService eventService;

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void postAsync(String eventName, Attributes attributes, Object... args) {
        getEventService().postAsync(eventName, attributes, getTimeout(), MILLISECONDS, args);
    }

    public EventService getEventService() {
        return eventService;
    }

    @Inject
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public long getTimeout() {
        return timeout;
    }

    @Inject
    public void setTimeout(@Named(EVENT_TIMEOUT_MSEC) long timeout) {
        this.timeout = timeout;
    }

}
