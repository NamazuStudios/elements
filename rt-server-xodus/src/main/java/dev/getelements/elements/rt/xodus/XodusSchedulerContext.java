package dev.getelements.elements.rt.xodus;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.id.TaskId;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static dev.getelements.elements.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT;
import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static jetbrains.exodus.env.StoreConfig.WITH_DUPLICATES_WITH_PREFIXING;

public class XodusSchedulerContext implements SchedulerContext {

    public static final String STORE_TIMER = "dev.getelements.elements.rt.xodus.scheduler";

    private static final Logger logger = LoggerFactory.getLogger(XodusSchedulerContext.class);

    private Environment environment;

    private ResourceService resourceService;

    private SimpleSchedulerContext simpleSchedulerContext;

    @Override
    public void start() {

        final var now = currentTimeMillis();
        final var environment = getEnvironment();

        getSimpleSchedulerContext().start();

        getEnvironment().executeInTransaction(txn -> {

            int count = 0;
            int failed = 0;
            int skipped = 0;
            final Store store = openStore(environment, txn);

            try (final Cursor cursor = store.openCursor(txn)) {
                while (cursor.getNext()) {

                    final ByteIterable key = cursor.getKey();
                    final ByteIterable value = cursor.getValue();
                    final XodusScheduledTask xodusScheduledTask = new XodusScheduledTask(key, value);

                    try {
                        if (getResourceService().exists(xodusScheduledTask.getTaskId().getResourceId())) {
                            schedule(now, xodusScheduledTask);
                            ++count;
                        } else {
                            ++skipped;
                            logger.debug("Skipping task {} because resource does not exist.", xodusScheduledTask.getTaskId());
                        }
                    } catch (Exception ex) {
                        logger.error("Got exception resuming task on disk.", ex);
                    } finally {
                        if (!cursor.deleteCurrent()) ++failed;
                    }

                }
            }

            logger.info("Restored {} scheduled tasks. Skipped {} tasks for nonexistent resources. {} Failed to Delete.",
                    count, skipped, failed);

        });

    }

    @Override
    public void stop() {
        getSimpleSchedulerContext().stop();
    }

    private void schedule(final long now, final XodusScheduledTask xodusScheduledTask) {

        final var environment = getEnvironment();
        final var delay = max(0, xodusScheduledTask.getWhen() - now);
        final var taskId = xodusScheduledTask.getTaskId();

        getSimpleSchedulerContext().resumeTaskAfterDelay(delay, MILLISECONDS, taskId, () -> environment.executeInTransaction(txn -> {
            final Store store = openStore(environment, txn);
            store.delete(txn, xodusScheduledTask.getValue());
        }));

    }

    @Override
    public void resume(TaskId taskId, Object... results) {
        getSimpleSchedulerContext().resume(taskId, results);
    }

    @Override
    public void resumeTaskAfterDelay(final TaskId taskId, final long time,
                                     final TimeUnit timeUnit) {

        // In case the process shuts down before tasks are executed, we must store the task info so it can be
        // re-run when the process wakes back up.

        final var environment = getEnvironment();
        final XodusScheduledTask xodusScheduledTask = new XodusScheduledTask(taskId, time, timeUnit);

        environment.executeInTransaction(txn -> {
            final Store store = openStore(environment, txn);
            store.put(txn, xodusScheduledTask.getKey(), xodusScheduledTask.getValue());
        });

        getSimpleSchedulerContext().resumeTaskAfterDelay(time, timeUnit, taskId,
            () -> environment.executeInTransaction(txn -> {
                final Store store = openStore(environment, txn);
                store.delete(txn, xodusScheduledTask.getValue());
            }));

    }

    @Override
    public void resumeFromNetwork(final TaskId taskId,
                                  final Object result) {
        getSimpleSchedulerContext().resumeFromNetwork(taskId, result);
    }

    @Override
    public void resumeWithError(final TaskId taskId,
                                final Throwable throwable) {
        getSimpleSchedulerContext().resumeWithError(taskId, throwable);
    }

    private Store openStore(final Environment environment, final Transaction txn) {
        return environment.openStore(STORE_TIMER, WITH_DUPLICATES_WITH_PREFIXING, txn);
    }

    private Environment getEnvironment() {
        return environment;
    }

    @Inject
    public void setEnvironment(@Named(SCHEDULER_ENVIRONMENT) Environment environment) {
        this.environment = environment;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Inject
    public void setResourceService(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public SimpleSchedulerContext getSimpleSchedulerContext() {
        return simpleSchedulerContext;
    }

    @Inject
    public void setSimpleSchedulerContext(SimpleSchedulerContext simpleSchedulerContext) {
        this.simpleSchedulerContext = simpleSchedulerContext;
    }

}
