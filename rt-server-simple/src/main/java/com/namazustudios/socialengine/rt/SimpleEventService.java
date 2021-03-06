package com.namazustudios.socialengine.rt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class SimpleEventService implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleEventService.class);

    private RetainedHandlerService retainedHandlerService;

    private ManifestLoader manifestLoader;

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void postAsync(final String eventName, final Attributes attributes,
                          long timeout, TimeUnit timeoutTimeUnit,
                          final Object... args) {

        final var eventManifest = getManifestLoader().getEventManifest();

        if (eventManifest == null) {
            logger.info("No event resources to run.  Skipping.");
            return;
        } else if (eventManifest.getModulesByEventName() == null) {
            logger.info("No event modules specified in manifest.  Skipping.");
            return;
        } else if(!eventManifest.getModulesByEventName().containsKey(eventName)){
            logger.info("No event modules matching name {} specified in manifest.  Skipping.", eventName);
            return;
        }

        final var eventOperations = eventManifest.getModulesByEventName().get(eventName);

        if (eventOperations == null) {
            logger.debug("Event module '{}' specifies no operation.  Skipping.", eventName);
            return;
        }

        for (final var operation : eventOperations) {

            final var method = operation.getMethod();
            final var module = operation.getModule();

            logger.debug("Executing event operation {}: {}.{}", eventName, module, method);

            final Consumer<Throwable> failure = ex -> {
                logger.error("Event exception caught for module: {}, method: {}.", module, method, ex);
            };

            final Consumer<Object> success = result -> {
                logger.debug("Event operation '{}: {}.{}': Success.", eventName, module, method);
            };

            getRetainedHandlerService().perform(success, failure,
                                                timeout, timeoutTimeUnit,
                                                module, attributes,
                                                method, args);

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

}
