package dev.getelements.elements.rt;

import dev.getelements.elements.sdk.cluster.id.ApplicationId;

import jakarta.inject.Named;

/**
 * Represents the connection the backend cluster of services.
 */
public interface Context {

    /**
     * Used with the {@link Named} annotation to designate context types which are local, as in they are not sent
     * via remote invocation.
     */
    String LOCAL = "dev.getelements.elements.rt.context.local";

    /**
     * Used with the {@link Named} annotation to designate context types which are remote, as in they are sent via
     * remote invocation.
     */
    String REMOTE = "dev.getelements.elements.rt.context.remote";

    /**
     * Starts the context.
     */
    void start();

    /**
     * Shuts down this {@link Context} and disconnecting this {@link Context}.  The default implementation simply
     * defers all work to the managed services.
     */
    void shutdown();

    /**
     * Gets the {@link ResourceContext} assocaited with this {@link Context}
     *
     * @return the {@link ResourceContext}
     */
    ResourceContext getResourceContext();

    /**
     * Gets the {@link SchedulerContext} assocaited with this {@link Context}
     *
     * @return the {@link SchedulerContext}
     */
    SchedulerContext getSchedulerContext();

    /**
     * Gets the {@link IndexContext} assocaited with this {@link Context}
     *
     * @return the {@link IndexContext}
     */
    IndexContext getIndexContext();

    /**
     * Gets the {@link HandlerContext}.
     *
     * @return the {@link HandlerContext}
     */
    HandlerContext getHandlerContext();

    /**
     * Gets the {@link TaskContext}.
     *
     * @return the {@link TaskContext}
     */
    TaskContext getTaskContext();

    /**
     * Gets the {@link EventContext}.
     *
     * @return the {@link EventContext}
     */
    EventContext getEventContext();

    /**
     * Gets the {@link ManifestContext} which provides metadata to about the application to the rest of the application.
     *
     * @return the {@link ManifestContext}
     */
    ManifestContext getManifestContext();

    /**
     * Builds a {@link Context} which can communicate with a specific application.
     */
    interface Factory {

        /**
         * Gets the {@link Context} for the supplied string representing the {@link ApplicationId}.
         *
         * @param applicationIdString The unique application name {@see {@link ApplicationId#forUniqueName(String)}}
         * @return the {@link Context}
         */
        default Context getContextForApplication(final String applicationIdString) {
            final var applicationId = ApplicationId.forUniqueName(applicationIdString);
            return getContextForApplication(applicationId);
        }

        /**
         * Gets a {@link Context} which can communicate with the remote application.
         *
         * @param applicationId the {@link ApplicationId} of the remote application
         * @return the {@link Context}
         */
        Context getContextForApplication(ApplicationId applicationId);

    }

}
