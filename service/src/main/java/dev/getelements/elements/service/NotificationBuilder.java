package dev.getelements.elements.service;

import dev.getelements.elements.exception.notification.NotificationConfigurationException;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.application.ApplicationConfiguration;
import dev.getelements.elements.model.profile.Profile;

import javax.annotation.Nonnull;
import java.util.Map;

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
     * Specifies the default sound to play when sending a message.  The default implementation just specifies the
     * sound using "default." passed to {@link #sound(String)}.
     *
     * @return this instance
     */
    default NotificationBuilder sound() {
        return sound("default");
    }

    /**
     * Specifies the sound to play when delivering the message.
     *
     * @param sound the sound to play when delivering the message
     * @return this instance
     */
    NotificationBuilder sound(String sound);

    /**
     * Adds a single key/value property.  Individual keys should be non-empty and all values should be non-null
     *
     * @param key - A non-empty string key for the property
     * @param value - A non-null string value for the property
     * @return this instance
     */
    NotificationBuilder add(@Nonnull String key, @Nonnull String value);

    /**
     * Adds all properties in the passed in mapping to the notification being built.  Any keys which are null or empty
     * Strings will be ignored.  Any values which are null will be ignored.  For all other entries, if any keys
     * previously exist in the notification, they will be replaced by the value specified in the passed in Map.
     * If a value is null, then that
     *
     * @param properties - A Map of all properties to be added in bulk to this notification.
     * @return this instance
     */
    NotificationBuilder addAll(Map<String,String> properties);

    /**
     * Constructs the instance of {@link Notification} with all of the configured information.  This returns a new
     * instance of {@link Notification} for each call which can be treated independently.
     *
     * @return the {@link Notification}
     */
    Notification build();

}
