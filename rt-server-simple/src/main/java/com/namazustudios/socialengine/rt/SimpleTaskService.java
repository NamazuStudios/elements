package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.DuplicateTaskException;
import com.namazustudios.socialengine.rt.exception.TaskKilledException;
import com.namazustudios.socialengine.rt.id.TaskId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class SimpleTaskService implements TaskService {

    private static final Logger logger = LoggerFactory.getLogger(SimpleTaskService.class);

    private final AtomicReference<ConcurrentMap<TaskId, Task>> taskMap = new AtomicReference<>();

    @Override
    public void start() {
        if (taskMap.compareAndSet(null, new ConcurrentHashMap<>())) {
            logger.info("Started TaskService.");
        } else {
            throw new IllegalStateException("SimpleTaskService already started.");
        }
    }

    @Override
    public void stop() {
        taskMap.getAndUpdate(m -> {

            if (m == null) {
                throw new IllegalStateException("SimpleTaskService already stopped.");
            }

            return null;

        }).forEach((taskId, task) -> {
            final TaskKilledException ex = new TaskKilledException(taskId);
            task.fail(ex);
        });
    }

    @Override
    public void register(final TaskId taskId,
                         final Consumer<Object> consumer,
                         final Consumer<Throwable> throwableTConsumer) {

        final Task task = new Task(taskId);
        task.register(consumer, throwableTConsumer);

        if (getMap().putIfAbsent(taskId, task) != null) {
            throw new DuplicateTaskException(taskId);
        }

    }

    @Override
    public boolean finishWithResult(final TaskId taskId, final Object result) {

        final Task task = getMap().remove(taskId);
        if (task == null) return false;

        task.finish(result);
        return true;

    }

    @Override
    public boolean finishWithError(final TaskId taskId, final Throwable error) {

        final Task task = getMap().remove(taskId);
        if (task == null) return false;

        task.fail(error);
        return true;

    }

    private ConcurrentMap<TaskId, Task> getMap() {
        final ConcurrentMap<TaskId, Task> map = taskMap.get();
        if (map == null) throw new IllegalStateException("SimpleTaskService not running.");
        return map;
    }

    private class Task {

        private final TaskId taskId;

        private final List<ResultConsumers> resultConsumerList = new ArrayList<>();

        private final AtomicBoolean finished = new AtomicBoolean();

        public Task(final TaskId taskId) {
            this.taskId = taskId;
        }

        public void register(final Consumer<Object> consumer,
                             final Consumer<Throwable> throwableTConsumer) {
            if (!finished.get()) {
                final ResultConsumers consumers = new ResultConsumers(taskId, consumer, throwableTConsumer);
                resultConsumerList.add(consumers);
            } else {
                logger.warn("Task {} already completed.  Cannot register more consumers.", taskId);
            }
        }

        private void finish(final Object result) {
            if (finished.compareAndSet(false, true)) {
                resultConsumerList.forEach(c -> c.finish(result));
            } else {
                logger.warn("Task {} already completed.  Cannot finish task.", taskId);
            }
        }

        private void fail(final Throwable throwable) {
            if (finished.compareAndSet(false, true)) {
                resultConsumerList.forEach(c -> c.fail(throwable));
            } else {
                logger.warn("Task {} already completed.  Cannot fail task.", taskId);
            }
        }

    }

    private class ResultConsumers {

        private final TaskId taskId;

        private final Consumer<Object> resultConsumer;

        private final Consumer<Throwable> throwableConsumer;

        public ResultConsumers(final TaskId taskId,
                               final Consumer<Object> resultConsumer,
                               final Consumer<Throwable> throwableConsumer) {
            this.taskId = taskId;
            this.resultConsumer = resultConsumer;
            this.throwableConsumer = throwableConsumer;
        }

        public void finish(final Object result) {
            try {
                resultConsumer.accept(result);
            } catch (Exception ex) {
                logger.error("Caught exception finishing task {}.", taskId, ex);
            }
        }

        public void fail(final Throwable error) {
            try {
                throwableConsumer.accept(error);
            } catch (Exception ex) {
                logger.error("Caught exception failing task {}.", taskId, ex);
            }
        }

    }

}
