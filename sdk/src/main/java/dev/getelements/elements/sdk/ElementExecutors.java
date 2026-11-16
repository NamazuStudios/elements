package dev.getelements.elements.sdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Platform-managed executor factory, bound in every Element's injector. Executors created through this
 * facility are tracked by the runtime and automatically shut down when the Element closes -- including
 * the hot-reload unload path -- so an Element's background work dies with its generation without
 * hand-rolled close hooks (#209).
 *
 * <p>Prefer this facility over hand-rolled {@code Executors.new...} calls: the platform cannot reach
 * executors it did not create, so a forgotten close hook on a hand-rolled executor re-creates the
 * zombie-executor failure class. Threads are named per executor (and per element), which makes stack
 * traces and triage unambiguous across element generations.
 *
 * <p>Example (Kotlin):
 * <pre>{@code
 * class MeteringSyncService @Inject constructor(
 *     elementExecutors: ElementExecutors,
 * ) {
 *     private val poller = elementExecutors.newScheduledExecutor("metering-sync", 1)
 * }
 * }</pre>
 */
public interface ElementExecutors {

    /**
     * Creates a tracked {@link ScheduledExecutorService} whose threads are named
     * {@code <element>:<name>-<n>}. The executor is shut down (interrupting running tasks) when the
     * owning Element closes.
     *
     * @param name a short, unique-per-element name identifying the executor in thread dumps
     * @param poolSize the core pool size
     * @return the managed executor
     */
    ScheduledExecutorService newScheduledExecutor(String name, int poolSize);

    /**
     * Creates a tracked {@link ExecutorService} whose threads are named {@code <element>:<name>-<n>}.
     * The executor is shut down (interrupting running tasks) when the owning Element closes.
     *
     * @param name a short, unique-per-element name identifying the executor in thread dumps
     * @return the managed executor
     */
    ExecutorService newExecutor(String name);

    /**
     * Shuts down every executor vended by this facility. Invoked by the runtime when the owning Element
     * closes; Element code should not call it.
     */
    void close();

}
