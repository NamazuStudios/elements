package dev.getelements.elements.service.notification;

import dev.getelements.elements.service.NotificationEvent;
import dev.getelements.elements.service.NotificationParameters;

public class StandardNotificationEvent implements NotificationEvent {

    private final String tokenId;

    private final NotificationParameters parameters;

    public StandardNotificationEvent(final String tokenId, final NotificationParameters parameters) {
        this.tokenId = tokenId;
        this.parameters = parameters;
    }

    @Override
    public String getTokenId() {
        return tokenId;
    }

    @Override
    public NotificationParameters getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return "StandardNotificationEvent{" +
                "tokenId='" + tokenId + '\'' +
                ", parameters=" + parameters +
                '}';
    }

}
