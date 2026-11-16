package dev.getelements.elements.sdk.spi.guice;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementExecutors;
import dev.getelements.elements.sdk.record.ElementRecord;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default {@link ElementExecutors} implementation, bound as an eager singleton in each isolated Element's
 * injector. Tracks every executor it vends; {@link #close()} shuts them all down and logs a single line --
 * the hot-swap evidence trail (#209).
 *
 * <p>Thread naming: {@code <element-name>:<executor-name>-<n>} so a stack trace identifies both the
 * poller and its Element generation.
 */
public class GuiceElementExecutors implements ElementExecutors {

    private static final Logger logger = LoggerFactory.getLogger(GuiceElementExecutors.class);

    private static final int TERMINATION_TIMEOUT_SECONDS = 5;

    private final ElementRecord elementRecord;

    private final List<ExecutorService> tracked = new ArrayList<>();

    private final Object lock = new Object();

    @Inject
    public GuiceElementExecutors(final ElementRecord elementRecord, final Element element) {

        this.elementRecord = elementRecord;

        // The reaper rides the element's own close-hook mechanism (#231): onClose subscriptions fire
        // before the classloader closes, so vended executors shut down while the element's archives are
        // still open. Registered from the constructor -- this impl is an eager singleton, and injection
        // (unlike the static supplier lookup) works during the injector build.
        element.onClose(el -> close());

    }

    @Override
    public ScheduledExecutorService newScheduledExecutor(final String name, final int poolSize) {
        final var executor = Executors.newScheduledThreadPool(poolSize, threadFactory(name));
        return track(name, executor);
    }

    @Override
    public ExecutorService newExecutor(final String name) {
        final var executor = Executors.newCachedThreadPool(threadFactory(name));
        return track(name, executor);
    }

    private <T extends ExecutorService> T track(final String name, final T executor) {
        synchronized (lock) {
            tracked.add(executor);
        }
        return executor;
    }

    private java.util.concurrent.ThreadFactory threadFactory(final String name) {

        final var elementName = elementRecord.definition().name();
        final var counter = new AtomicInteger();

        return r -> {
            final var thread = new Thread(r, "%s:%s-%d".formatted(elementName, name, counter.incrementAndGet()));
            thread.setDaemon(true);
            return thread;
        };

    }

    @Override
    public void close() {

        final List<ExecutorService> toReap;

        synchronized (lock) {
            if (tracked.isEmpty()) {
                return;
            }
            toReap = List.copyOf(tracked);
            tracked.clear();
        }

        for (final var executor : toReap) {
            executor.shutdownNow();
        }

        for (final var executor : toReap) {
            try {
                executor.awaitTermination(TERMINATION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        logger.info("Reaped {} executor(s) for element {}",
                toReap.size(), elementRecord.definition().name());

    }

}
