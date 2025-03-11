package dev.getelements.elements.rt.transact;

import dev.getelements.elements.sdk.cluster.id.TaskId;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractTaskEntry<ScopeT> implements TaskEntry<ScopeT> {

    private final OperationalStrategy<ScopeT> operationalStrategy;

    public AbstractTaskEntry(final OperationalStrategy<ScopeT> operationalStrategy) {
        this.operationalStrategy = operationalStrategy;
    }

    @Override
    public Map<TaskId, TransactionalTask> getTasksImmutable() {
        return operationalStrategy.doGetTasksImmutable(this);
    }

    @Override
    public boolean deleteTask(TaskId taskId) {
        return operationalStrategy.doDeleteTask(this, taskId);
    }

    @Override
    public boolean addTask(final TaskId taskId, final long timestamp) {
        return operationalStrategy.doAddTask(this, taskId, timestamp);
    }

    @Override
    public Optional<ScopeT> findScope() {
        return operationalStrategy.doFindScope(this);
    }

    @Override
    public boolean delete() {
        return operationalStrategy.doDelete(this);
    }

}

