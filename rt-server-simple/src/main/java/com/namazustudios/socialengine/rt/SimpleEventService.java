package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.manifest.event.EventManifest;
import com.namazustudios.socialengine.rt.manifest.event.EventOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.namazustudios.socialengine.rt.EventContext.EVENT_TIMEOUT_MSEC;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class SimpleEventService implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventService.class);

    private RetainedHandlerService retainedHandlerService;

    private ManifestLoader manifestLoader;

    private long timeout;

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void postAsync(String eventName, Attributes attributes, Object... args) {
        final EventManifest eventManifest = getManifestLoader().getEventManifest();

        if (eventManifest == null) {
            logger.info("No event resources to run.  Skipping.");
            return;
        } else if (eventManifest.getModulesByEventName() == null) {
            logger.info("No event modules specified in manifest.  Skipping.");
            return;
        }

        for (final Map.Entry<String, List<EventOperation>> entry : eventManifest.getModulesByEventName().entrySet()) {

            final List<EventOperation> eventOperations = entry.getValue();

            if (eventOperations == null) {
                logger.debug("Event module '{}' specifies no operation.  Skipping.", entry.getKey());
                continue;
            }

            for (final EventOperation operation : eventOperations) {

                final String method = operation.getMethod();
                final String module = operation.getModule();
                logger.debug("Executing event operation {}: {}.{}", eventName, module, method);

                final Consumer<Throwable> failure = ex -> {
                    logger.error("Event exception caught for module: {}, method: {}.", module, method, ex);
                };

                final Consumer<Object> success = result -> {
                    logger.debug("Event operation '{}: {}.{}': Success.", eventName, module, method);
                };

                getRetainedHandlerService().perform(success, failure, getTimeout(), MILLISECONDS, module, attributes, method, args);

            }
        }
    }

    public RetainedHandlerService getRetainedHandlerService() {
        return retainedHandlerService;
    }

    @Inject
    public void setRetainedHandlerService(RetainedHandlerService retainedHandlerService) {
        this.retainedHandlerService = retainedHandlerService;
    }

    public ManifestLoader getManifestLoader() {
        return manifestLoader;
    }

    @Inject
    public void setManifestLoader(ManifestLoader manifestLoader) {
        this.manifestLoader = manifestLoader;
    }

    public long getTimeout() {
        return timeout;
    }

    @Inject
    public void setTimeout(@Named(EVENT_TIMEOUT_MSEC) long timeout) {
        this.timeout = timeout;
    }
}
