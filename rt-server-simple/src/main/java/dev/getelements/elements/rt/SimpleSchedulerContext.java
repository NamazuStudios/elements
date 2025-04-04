package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.NoSuchTaskException;
import dev.getelements.elements.sdk.cluster.id.TaskId;
import dev.getelements.elements.sdk.util.FinallyAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.concurrent.TimeUnit;

public class SimpleSchedulerContext implements SchedulerContext {

    private static final Logger logger = LoggerFactory.getLogger(SimpleSchedulerContext.class);

    private Scheduler scheduler;

    private TaskService taskService;

    @Override
    public void start() {
        getScheduler().start();
    }

    @Override
    public void stop() {
        getScheduler().stop();
    }

    @Override
    public void resume(final TaskId taskId, final Object ... results) {
        getScheduler().performV(taskId.getResourceId(),
            txn -> FinallyAction.begin(logger)
                    .then(() -> logger.trace("Resumed task {}:{}", taskId))
                    .then(() -> txn.getResource().resume(taskId, results))
                .run(),
            th -> FinallyAction.begin(logger)
                    .then(() -> logger.error("Caught exception resuming {}.", taskId, th))
                    .then(() -> getTaskService().finishWithError(taskId, th))
                .run()
        );
    }

    @Override
    public void resumeTaskAfterDelay(final TaskId taskId, final long time, final TimeUnit timeUnit) {
        getScheduler().resumeTaskAfterDelay(
            taskId, time, timeUnit,
            () -> FinallyAction.begin(logger)
                    .then(() -> logger.trace("Resumed task {}", taskId))
                .run(),
            th -> FinallyAction.begin(logger)
                    .then(() -> logger.error("Caught exception resuming {}.", taskId, th))
                    .then(() -> getTaskService().finishWithError(taskId, th))
                .run()
        );
    }

    public void resumeTaskAfterDelay(long time, final TimeUnit timeUnit,
                                     final TaskId taskId,
                                     final Runnable resumed) {
        getScheduler().resumeTaskAfterDelay(
            taskId, time, timeUnit,
            () -> FinallyAction.begin(logger)
                    .then(() -> logger.trace("Resumed task {}", taskId))
                    .then(resumed)
                .run(),
            th -> FinallyAction.begin(logger)
                    .then(() -> logger.error("Caught exception resuming {}.", taskId, th))
                    .then(() -> getTaskService().finishWithError(taskId, th))
                    .then(resumed)
                .run()
            );
    }

    @Override
    public void resumeFromNetwork(final TaskId taskId, final Object result) {
        getScheduler().performV(taskId.getResourceId(),
            txn -> resumeFromNetwork(txn.getResource(), taskId, result),
            th -> FinallyAction.begin(logger)
                    .then(() -> logger.error("Caught exception resuming {}.", taskId, th))
                    .then(() -> getTaskService().finishWithError(taskId, th))
                .run()
        );
    }

    private void resumeFromNetwork(final Resource resource, final TaskId taskId, final Object result) {
        try {
            resource.resumeFromNetwork(taskId, result);
        } catch (NoSuchTaskException ex) {
            logger.debug("Ignoring dead task: {}", ex.getTaskId());
        }
    }

    @Override
    public void resumeWithError(final TaskId taskId, final Throwable throwable) {
        getScheduler().performV(taskId.getResourceId(),
            txn -> resumeWithError(txn.getResource(), taskId, throwable),
            th -> logger.error("Caught exception resuming.", th));
    }

    private void resumeWithError(final Resource resource, final TaskId taskId, final Throwable throwable) {
        try {
            resource.resumeWithError(taskId, throwable);
        } catch (NoSuchTaskException ex) {
            logger.debug("Ignoring dead task: {}", ex.getTaskId());
        }
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    @Inject
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    @Inject
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

}
