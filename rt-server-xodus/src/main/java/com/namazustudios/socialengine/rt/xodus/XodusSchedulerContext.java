package com.namazustudios.socialengine.rt.xodus;

import com.namazustudios.socialengine.rt.*;
import jetbrains.exodus.ByteIterable;
import jetbrains.exodus.env.Cursor;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Store;
import jetbrains.exodus.env.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static jetbrains.exodus.env.StoreConfig.WITH_DUPLICATES_WITH_PREFIXING;

public class XodusSchedulerContext implements SchedulerContext {

    public static final String STORE_TIMED = "scheduled";

    private static final Logger logger = LoggerFactory.getLogger(XodusSchedulerContext.class);

    private Environment environment;

    private SimpleSchedulerContext simpleSchedulerContext;

    @Override
    public void start() {

        final long now = currentTimeMillis();

        getEnvironment().executeInTransaction(txn -> {

            int count = 0;
            final Store store = openStore(txn);

            try (final Cursor cursor = store.openCursor(txn)) {
                while (cursor.getNext()) {
                    final ByteIterable key = cursor.getKey();
                    final ByteIterable value = cursor.getValue();
                    final XodusScheduledTask xodusScheduledTask = new XodusScheduledTask(key, value);
                    schedule(now, xodusScheduledTask);
                    ++count;
                }
            }

            logger.info("Restored {} scheduled tasks.", count);

        });

    }

    private void schedule(final long now, final XodusScheduledTask xodusScheduledTask) {
        //TODO Restore scheduled tasks.
    }

    @Override
    public void resumeTaskAfterDelay(final ResourceId resourceId,
                                     final long time,
                                     final TimeUnit timeUnit,
                                     final TaskId taskId) {

        // In case the process shuts down before tasks are executed, we must store the task info so it can be
        // re-run when the process wakes back up.
        final XodusScheduledTask xodusScheduledTask = new XodusScheduledTask(resourceId, taskId, time, timeUnit);

        getEnvironment().executeInTransaction(txn -> {
            final Store store = openStore(txn);
            store.put(txn, xodusScheduledTask.getKey(), xodusScheduledTask.getValue());
        });

        getSimpleSchedulerContext().resumeTaskAfterDelay(resourceId, time, timeUnit, taskId, () -> getEnvironment().executeInTransaction(txn -> {
            final Store store = openStore(txn);
            store.delete(txn, xodusScheduledTask.getValue());
        }));

    }

    @Override
    public void resumeFromNetwork(final ResourceId resourceId,
                                  final TaskId taskId,
                                  final Object result) {
        getSimpleSchedulerContext().resumeFromNetwork(resourceId, taskId, result);
    }

    @Override
    public void resumeWithError(final ResourceId resourceId,
                                final TaskId taskId,
                                final Throwable throwable) {
        getSimpleSchedulerContext().resumeWithError(resourceId, taskId, throwable);
    }

    private Store openStore(final Transaction txn) {
        return getEnvironment().openStore(STORE_TIMED, WITH_DUPLICATES_WITH_PREFIXING, txn);
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Inject
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public SimpleSchedulerContext getSimpleSchedulerContext() {
        return simpleSchedulerContext;
    }

    @Inject
    public void setSimpleSchedulerContext(SimpleSchedulerContext simpleSchedulerContext) {
        this.simpleSchedulerContext = simpleSchedulerContext;
    }

}
