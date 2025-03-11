package dev.getelements.elements.rt.transact;

import dev.getelements.elements.rt.ResourceService;
import dev.getelements.elements.rt.SchedulerContext;
import dev.getelements.elements.rt.SimpleSchedulerContext;
import dev.getelements.elements.rt.exception.ResourceNotFoundException;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.lang.Math.max;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TransactionalSchedulerContext implements SchedulerContext {

    private static final Logger logger = LoggerFactory.getLogger(TransactionalSchedulerContext.class);

    private ResourceService resourceService;

    private SimpleSchedulerContext simpleSchedulerContext;

    private TransactionalResourceServicePersistence persistence;

    @Override
    public void start() {

        getSimpleSchedulerContext().start();

        final List<TransactionalTask> taskList;

        try (final var txn = getPersistence().openExclusiveRW();
             final var taskStream = txn.computeOperation(ds -> ds.getTaskIndex().listAllTasks())) {
            taskList = taskStream.collect(Collectors.toList());
        }

        taskList.forEach(task -> resumeTaskAfterDelay(
                task.getTaskId(),
                max(task.getTimestamp() - currentTimeMillis(), 0),
                MILLISECONDS
        ));

    }

    @Override
    public void stop() {
        getSimpleSchedulerContext().stop();
    }

    @Override
    public void resume(TaskId taskId, Object... results) {
        getSimpleSchedulerContext().resume(taskId, results);
    }

    @Override
    public void resumeTaskAfterDelay(final TaskId taskId,
                                     final long time,
                                     final TimeUnit timeUnit) {

        final var nodeId = taskId.getNodeId();
        final var absolute = MILLISECONDS.convert(time, timeUnit) + currentTimeMillis();

        final Supplier<ReadWriteTransaction> txnSupplier = () -> getPersistence()
                .buildRW(nodeId)
                .with(taskId.getResourceId())
                .begin();

        try (final var txn = txnSupplier.get()) {
            txn.createTask(taskId, absolute);
            txn.commit();
        } catch (ResourceNotFoundException ex) {
            logger.debug("No such resource for task {}", taskId);
        }

        getSimpleSchedulerContext().resumeTaskAfterDelay(time, timeUnit, taskId,
                () -> {
                    try (final var txn = txnSupplier.get()) {
                        txn.deleteTask(taskId);
                        txn.commit();
                    }
                });

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

    public TransactionalResourceServicePersistence getPersistence() {
        return persistence;
    }

    @Inject
    public void setPersistence(TransactionalResourceServicePersistence persistence) {
        this.persistence = persistence;
    }

}
