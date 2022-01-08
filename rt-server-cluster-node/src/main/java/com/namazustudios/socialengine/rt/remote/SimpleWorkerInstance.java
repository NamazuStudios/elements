package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.PersistenceEnvironment;
import com.namazustudios.socialengine.rt.exception.MultiException;
import com.namazustudios.socialengine.rt.id.ApplicationId;
import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.rt.remote.Node.MASTER_NODE_NAME;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

/**
 * Contains all {@link Node} instances for several {@link Node} instances and manages their life cycles therein.  This
 * imposes the additional requirement of providing some form of {@link InstanceConnectionService} to route internal
 * requests.
 */
public class SimpleWorkerInstance extends SimpleInstance implements Worker {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWorkerInstance.class);

    private Node masterNode;

    private Set<Node> nodeSet;

    private PersistenceEnvironment persistenceEnvironment;

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;

    private Node.Factory nodeFactory;

    private final Set<InstanceBinding> bindingSet = new HashSet<>();

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    @Override
    public InstanceId getInstanceId() {
        return instanceId;
    }

    @Override
    protected void postStart(final Consumer<Exception> exceptionConsumer) {

        try {
            getPersistence().start();
        } catch (Exception ex) {
            logger.error("Could not start worker instance persistence.", ex);
            exceptionConsumer.accept(ex);
        }

        final var lock = rwLock.writeLock();

        try {
            lock.lock();
            doPostStart(exceptionConsumer);
        } finally {
            lock.unlock();
        }

    }

    private void doPostStart(final Consumer<Exception> exceptionConsumer) {

        final var startupList = concat(of(getMasterNode()), getNodeSet().stream()).map(node -> {
            try {
                logger.debug("Beginning node startup for node {}", node.getNodeId());
                return node.beginStartup();
            } catch (Exception ex) {
                logger.error("Error beginning node startup process.", ex);
                exceptionConsumer.accept(ex);
                return null;
            }
        }).filter(Objects::nonNull).collect(toList());

        doStartNodes(startupList, exceptionConsumer);

    }

    private List<Node> doStartNodes(List<Node.Startup> startupList, Consumer<Exception> exceptionConsumer) {

        startupList = startupList.stream().map(s -> {
            try {
                logger.debug("Executing pre-start operations for node {}", s.getNodeId());
                s.preStart();
                return s;
            } catch (Exception ex) {
                logger.error("Error in node pre-startup process.", ex);
                exceptionConsumer.accept(ex);
                s.cancel();
                return null;
            }
        }).filter(Objects::nonNull).collect(toList());

        startupList = startupList.stream().map(s -> {

            logger.debug("Opening binding for node id {}", s.getNodeId());

            final var binding = getInstanceConnectionService().openBinding(s.getNodeId());
            bindingSet.add(binding);

            logger.debug("Opened binding for node {}.", s.getNodeId());

            try {
                logger.debug("Executing start operations for node {}", s.getNodeId());
                s.start(binding);
                return s;
            } catch (Exception ex) {
                logger.error("Error in node startup process.", ex);
                exceptionConsumer.accept(ex);
                s.cancel();
                binding.close();
                return null;
            }
        }).filter(Objects::nonNull).collect(toList());

        startupList.stream().map(s -> {
            try {
                logger.debug("Executing post-start operations for node {}.", s.getNodeId());
                s.postStart();
                return null;
            } catch (Exception ex) {
                logger.error("Error in node post-startup process.", ex);
                exceptionConsumer.accept(ex);
                s.cancel();
                return ex;
            }
        }).filter(Objects::nonNull).forEach(exceptionConsumer);

        return startupList
            .stream()
            .map(Node.Startup::getNode)
            .collect(toList());

    }

    @Override
    protected void preClose(final Consumer<Exception> exceptionConsumer) {
        doShutdownNodes(concat(of(getMasterNode()), getNodeSet().stream()), exceptionConsumer);
    }

    private void doShutdownNodes(Stream<Node> shutdownStream, Consumer<Exception> exceptionConsumer) {

        final var shutdownList = shutdownStream.map(node -> {
            try {
                return node.beginShutdown();
            } catch (Exception ex) {
                logger.error("Error beginning node shutdown process.", ex);
                exceptionConsumer.accept(ex);
                return null;
            }
        }).filter(Objects::nonNull).collect(toList());

        shutdownList.forEach(s -> {
            try {
                s.preStop();
            } catch (Exception ex) {
                logger.error("Error issuing node pre-shutdown process.", ex);
                exceptionConsumer.accept(ex);
            }
        });

        shutdownList.forEach(s -> {
            try {
                s.stop();
            } catch (Exception ex) {
                logger.error("Error issuing node shutdown process.", ex);
                exceptionConsumer.accept(ex);
            }
        });

        shutdownList.forEach(s -> {
            try {
                s.postStop();
            } catch (Exception ex) {
                logger.error("Error issuing node post-shutdown process.", ex);
                exceptionConsumer.accept(ex);
            }
        });

        bindingSet.stream().map(binding -> {
            try {
                binding.close();
                return null;
            } catch (Exception ex) {
                SimpleWorkerInstance.logger.error("Error closing binding {}.", binding, ex);
                return ex;
            }
        }).filter(Objects::nonNull).forEach(exceptionConsumer);

    }

    @Override
    protected void postClose(Consumer<Exception> exceptionConsumer) {

        logger.info("Shutting down scheduler threads.");
        getExecutorService().shutdown();

        logger.info("Shutting down dispatcher threads.");
        getScheduledExecutorService().shutdown();

        try {
            if (getScheduledExecutorService().awaitTermination(5, MINUTES)) {
                logger.info("Shut down scheduler threads.");
            } else {
                logger.error("Timed out shutting down scheduler threads.");
            }
        } catch (InterruptedException ex) {
            logger.error("Interrupted while shutting down scheduler threads.", ex);
            exceptionConsumer.accept(ex);
        }

        try {
            if (getScheduledExecutorService().awaitTermination(5, TimeUnit.MINUTES)) {
                logger.info("Shut down worker threads.");
            } else {
                logger.error("Timed out shutting down worker threads.");
            }
        } catch (InterruptedException ex) {
            logger.error("Interrupted while shutting down worker threads.", ex);
            exceptionConsumer.accept(ex);
        }

        try {
            getPersistence().stop();
        } catch (Exception ex) {
            logger.error("Could not stop worker instance persistence.", ex);
            exceptionConsumer.accept(ex);
        }

    }

    @Override
    public Accessor accessWorkerState() {

        final var rLock = rwLock.readLock();
        rLock.lock();

        return new Accessor() {

            boolean locked = true;

            @Override
            public Set<Node> getNodeSet() {
                check();
                return new HashSet<>(nodeSet);
            }

            @Override
            public Set<InstanceBinding> getBindingSet() {
                check();
                return new HashSet<>(bindingSet);
            }

            @Override
            public void close() {
                if (locked) {
                    locked = false;
                    rLock.unlock();
                }
            }

            private void check() {
                if (!locked) throw new IllegalStateException("The accessor is closed.");
            }

        };
    }

    @Override
    public Mutator beginMutation() {

        final var wLock = rwLock.writeLock();
        wLock.lock();


        return new Mutator() {

            boolean locked = true;

            final Set<ApplicationId> toAdd = new HashSet<>();

            final Set<ApplicationId> toRemove = new HashSet<>();

            final Set<ApplicationId> existing = nodeSet
                .stream()
                .map(n -> n.getNodeId().getApplicationId())
                .collect(toSet());

            @Override
            public Set<Node> getNodeSet() {
                return new HashSet<>(nodeSet);
            }

            @Override
            public Set<InstanceBinding> getBindingSet() {
                check();
                return new HashSet<>(bindingSet);
            }

            @Override
            public Mutator addNode(final ApplicationId applicationId) {

                check();

                if (existing.contains(applicationId)) {
                    throw new IllegalArgumentException("Application already exists: " + applicationId);
                } else if (!toAdd.add(applicationId)) {
                    throw new IllegalArgumentException("Application already added for this Mutation: " + applicationId);
                }

                return this;

            }

            @Override
            public Mutator restartNode(final ApplicationId applicationId) {

                if (!existing.contains(applicationId)) {
                    throw new IllegalArgumentException("Application does not exist: "  + applicationId);
                } else if (toAdd.contains(applicationId)) {
                    throw new IllegalArgumentException("Application already slated for addition: "  + applicationId);
                } else if (!toRemove.add(applicationId)) {
                    throw new IllegalArgumentException("Application already slated for removal: " + applicationId);
                } else {
                    toAdd.add(applicationId);
                }

                return this;

            }

            @Override
            public Mutator removeNode(final ApplicationId applicationId) {

                if (!existing.contains(applicationId)) {
                    throw new IllegalArgumentException("Application does not exist: "  + applicationId);
                } else if (toAdd.contains(applicationId)) {
                    throw new IllegalArgumentException("Application already slated for addition: "  + applicationId);
                } else if (!toRemove.add(applicationId)) {
                    throw new IllegalArgumentException("Application already slated for removal: " + applicationId);
                }

                return this;

            }

            @Override
            public Mutator commit() {

                check();

                final var exceptions = new ArrayList<Exception>();

                final var toStop = nodeSet
                    .stream()
                    .filter(n -> toRemove.contains(n.getNodeId().getApplicationId()))
                    .collect(toList());

                doShutdownNodes(toStop.stream(), exceptions::add);

                final var toStart = toAdd.stream()
                    .map(getNodeFactory()::create)
                    .map(Node::beginStartup)
                    .collect(toList());

                final var started = doStartNodes(toStart, exceptions::add);

                nodeSet.removeAll(toStop);
                nodeSet.addAll(started);
                refresh();

                if (!exceptions.isEmpty()) {
                    throw new MultiException(exceptions);
                }

                return this;

            }

            @Override
            public void close() {
                if (locked) {
                    wLock.unlock();
                    locked = false;
                }
            }

            private void refresh() {
                toAdd.clear();
                toRemove.clear();
                existing.clear();
                nodeSet.forEach(n -> existing.add(n.getNodeId().getApplicationId()));
            }

            private void check() {
                if (!locked) throw new IllegalStateException("The mutation is closed.");
            }

        };
    }

    public Set<Node> getNodeSet() {
        return nodeSet;
    }

    public PersistenceEnvironment getPersistence() {
        return persistenceEnvironment;
    }

    @Inject
    public void setPersistence(PersistenceEnvironment persistenceEnvironment) {
        this.persistenceEnvironment = persistenceEnvironment;
    }

    @Inject
    public void setNodeSet(Set<Node> nodeSet) {
        this.nodeSet = new HashSet<>(nodeSet);
    }

    public Node getMasterNode() {
        return masterNode;
    }

    @Inject
    public void setMasterNode(@Named(MASTER_NODE_NAME) Node masterNode) {
        this.masterNode = masterNode;
    }

    @Inject
    public void setInstanceId(InstanceId instanceId) {
        this.instanceId = instanceId;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Inject
    public void setExecutorService(@Named(EXECUTOR_SERVICE) ExecutorService executorService) {
        this.executorService = executorService;
    }

    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Inject
    public void setScheduledExecutorService(@Named(SCHEDULED_EXECUTOR_SERVICE) ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = scheduledExecutorService;
    }

    public Node.Factory getNodeFactory() {
        return nodeFactory;
    }

    @Inject
    public void setNodeFactory(final Node.Factory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

}
