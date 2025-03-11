package dev.getelements.elements.rt;

import dev.getelements.elements.rt.ResourceService.ResourceAcquisition;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.sdk.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;

public class SimpleSingleUseHandlerService implements SingleUseHandlerService {

    private static final int PURGE_BATCH_SIZE = 100;

    private static final Logger logger = LoggerFactory.getLogger(SimpleSingleUseHandlerService.class);

    private Scheduler scheduler;

    private ResourceLoader resourceLoader;

    private ResourceService resourceService;

    private final AtomicBoolean running = new AtomicBoolean();

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            purge();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            purge();
        } else {
            throw new IllegalStateException("Already started.");
        }
    }

    private void purge() {

        final Path path = Path.fromComponents("tmp", "handler", "su", "*");

        List<ResourceService.Unlink> unlinkList;

        do {
            unlinkList = getResourceService().unlinkMultiple(path, PURGE_BATCH_SIZE);
            logger.info("Purged {} resources.", unlinkList.size());
            logger.debug("Purged [{}]", unlinkList);
        } while (!unlinkList.isEmpty());

    }

    @Override
    public TaskId perform(final Consumer<Object> success, final Consumer<Throwable> failure,
                          long timeoutDelay, TimeUnit timeoutUnit,
                          final String module, final Attributes attributes,
                          final String method, final Object... args) {

        final var path = Path.fromComponents("tmp", "handler", "su", randomUUID().toString());

        try (var acquisition = acquire(path, module, attributes)) {

            final var sent = new AtomicBoolean();
            final var resourceId = acquisition.getResourceId();
            final var destroy = getScheduler().scheduleDestruction(resourceId, timeoutDelay, timeoutUnit);

            final Consumer<Throwable> _failure = t -> {
                try {

                    final var _args = stream(args)
                            .map(a -> a == null ? "null" : a.toString())
                            .collect(Collectors.joining(","));

                    logger.error("Caught exception processing single-use handler {}.{}({}).", module, method, _args, t);
                    if (sent.compareAndSet(false, true)) failure.accept(t);

                } catch (Exception ex) {
                    logger.error("Caught exception sending response from resource {}", resourceId, ex);
                } finally {
                    destroy.run();
                }
            };

            final Consumer<Object> _success = o -> {
                try {
                    if (sent.compareAndSet(false, true)) success.accept(o);
                } catch (Throwable th) {
                    _failure.accept(th);
                } finally {
                    destroy.run();
                }
            };

            try (var txn = acquisition.begin()) {
                return txn
                        .getResource()
                        .getMethodDispatcher(method)
                        .params(args)
                        .dispatch(_success, _failure);
            }

        }

    }

    private ResourceAcquisition acquire(final Path path, final String module, final Attributes attributes) {
        final Resource resource = getResourceLoader().load(module, attributes);
        return getResourceService().addAndAcquireResource(path, resource);
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Inject
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

}
