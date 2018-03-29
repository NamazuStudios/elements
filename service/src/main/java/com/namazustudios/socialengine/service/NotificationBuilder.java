package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.exception.NotificationConfigurationException;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Builds instances of {@link Notification} based on input parameters.  Instances of {@link NotificationBuilder} may
 * be configured partially by the container, therefore they should be created using a {@link javax.inject.Provider} or
 * {@link javax.inject.Inject}ed into client code.
 *
 * In general a new instance of {@link NotificationBuilder} should be obtained for each use as they should not be
 * considered thread-safe.   Likewise, they should not be bound in the container as {@link javax.inject.Singleton} types
 * and should be created on-demand.
 *
 * Typically instances of this interface will throw {@link NotificationConfigurationException} in the event
 * notifications are not configured properly, such as missing the correct {@link ApplicationConfiguration} assocaited
 * with the requesting {@link Application}.
 *
 */
public interface NotificationBuilder {

    /**
     * Specifies the profile which is sending the notification.  The default implementation of this should just defaults
     * to using the {@link Profile#getApplication()} and supplying it directly to {@link Application}.  Implementations
     * may include more detailed information about the sender of the {@link Notification}
     *
     * @param sourceProfile the {@link Profile} of the source of the {@link Notification}
     * @return this instance
     */
    default NotificationBuilder sender(final Profile sourceProfile) {
        return application(sourceProfile.getApplication());
    }

    /**
     * Specifies the {@link Application} sourcing the {@link Notification}.  This will scan the associated
     * {@link ApplicationConfiguration} instances and select a suitable configuration (or combination thereof) which
     * will be used to configure the means by which the underlying {@link Notification} will be sent.
     *
     * @param application the {@link Application} sending the {@link Notification}
     * @return this instance
     */
    NotificationBuilder application(Application application);

    /**
     * Specifies the {@link Profile} which will receive the {@link Notification}.
     *
     * @param recipient the {@link Profile} which will recieve the {@link Notification}
     * @return this instance
     */
    NotificationBuilder recipient(Profile recipient);

    /**
     * Specifies the title text of the {@link Notification}.
     *
     * @param title the title text
     * @return this instance
     */
    NotificationBuilder title(String title);

    /**
     * Specifies the text to send along with the {@link Notification}.
     *
     * @param message the message to send
     * @return this instance
     */
    NotificationBuilder message(String message);

    /**
     * Constructs the instance of {@link Notification} with all of the configured information.  This returns a new
     * instance of {@link Notification} for each call which can be treated independently.
     *
     * @return the {@link Notification}
     */
    Notification build();

}
