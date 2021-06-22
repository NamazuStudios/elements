package com.namazustudios.socialengine.service.health;

import com.namazustudios.socialengine.dao.DatabaseHealthStatusDao;
import com.namazustudios.socialengine.model.health.*;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.remote.*;
import com.namazustudios.socialengine.rt.remote.RemoteInvokerRegistry.RemoteInvokerStatus;
import com.namazustudios.socialengine.service.HealthStatusService;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class DefaultHealthStatusService implements HealthStatusService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHealthStatusService.class);

    private Mapper mapper;

    private InstanceDiscoveryService instanceDiscoveryService;

    private RemoteInvokerRegistry remoteInvokerRegistry;

    private Provider<ControlClient> controlClientProvider;

    private Set<DatabaseHealthStatusDao> databaseHealthStatusDaos;

    @Override
    public HealthStatus checkHealthStatus() {
        return new HealthChecklist()
                .with(this::checkDatabaseStatus)
                .with(this::checkDiscoveryStatus)
                .with(this::checkInstanceConnectionStatus)
                .with(this::checkRoutingStatus)
                .with(this::checkRemoteInvokerStatus)
            .run();
    }

    private void checkDatabaseStatus(final HealthChecklist healthChecklist) {

        final var databaseHealthStatus = getDatabaseHealthStatusDaos()
            .stream()
            .map(DatabaseHealthStatusDao::checkDatabaseHealthStatus)
            .collect(toList());

        healthChecklist.getHealthStatus().setDatabaseStatus(databaseHealthStatus);

    }

    private void checkDiscoveryStatus(final HealthChecklist healthChecklist) {

        final var knownHosts = getInstanceDiscoveryService().getKnownHosts();
        final var discoveryHealthStatus = new DiscoveryHealthStatus();

        discoveryHealthStatus.setRecords(knownHosts
            .stream()
            .map(Object::toString)
            .collect(toList()));

        discoveryHealthStatus.setKnownHosts(knownHosts
            .stream()
            .map(InstanceHostInfo::getConnectAddress)
            .collect(toList()));

        healthChecklist.getHealthStatus().setDiscoveryHealthStatus(discoveryHealthStatus);

    }

    private void checkInstanceConnectionStatus(final HealthChecklist healthChecklist) {
        try (var client = getControlClientProvider().get()) {
            final var routingStatus = getMapper().map(client.getRoutingStatus(), RoutingHealthStatus.class);
            final var instanceStatus = getMapper().map(client.getInstanceStatus(), InstanceHealthStatus.class);
            healthChecklist.getHealthStatus().setInstanceStatus(instanceStatus);
            healthChecklist.getHealthStatus().setRoutingHealthStatus(routingStatus);
        }
    }

    private void checkRoutingStatus(final HealthChecklist healthChecklist) {

        final var routingTable = healthChecklist
            .getHealthStatus()
            .getRoutingHealthStatus()
            .getRoutingTable();

        healthChecklist.getHealthStatus()
            .getDiscoveryHealthStatus()
            .getKnownHosts()
            .forEach(known -> healthChecklist.with(hcl -> routingTable
                .stream()
                .filter(route -> route.contains(known))
                .findAny()
                .orElseThrow(() -> new InternalException(known + " is not connected."))));

    }

    private void checkRemoteInvokerStatus(final HealthChecklist healthChecklist) {

        final var priorities = getRemoteInvokerRegistry()
            .getAllRemoteInvokerStatus()
            .stream()
            .sorted((s0, s1) -> {
                final var nodeIdCmp = s0.getNodeId().compareTo(s1.getNodeId());
                if (nodeIdCmp != 0) return nodeIdCmp;
                return Double.compare(s1.getPriority(), s0.getPriority());
            })
            .map(s -> format("%s %s: %s", s.getNodeId(), s.getInvoker().getConnectAddress(), s.getPriority()))
            .collect(toList());

        final var connectedPeers = getRemoteInvokerRegistry()
            .getAllRemoteInvokerStatus()
            .stream()
            .map(s -> s.getInvoker().getConnectAddress())
            .collect(toList());

        final var healthStatus = healthChecklist.getHealthStatus();
        final var invokerHealthStatus = new InvokerHealthStatus();

        invokerHealthStatus.setPriorities(priorities);
        invokerHealthStatus.setConnectedPeers(connectedPeers);

        healthStatus
            .getDiscoveryHealthStatus()
            .getKnownHosts()
            .forEach(known -> healthChecklist.with(hcl -> connectedPeers
                .stream()
                .filter(peer -> peer.contains(known))
                .findAny()
                .orElseThrow(() -> new InternalException(known + " is not connected."))));

        healthStatus.setInvokerHealthStatus(invokerHealthStatus);

    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public Provider<ControlClient> getControlClientProvider() {
        return controlClientProvider;
    }

    @Inject
    public void setControlClientProvider(Provider<ControlClient> controlClientProvider) {
        this.controlClientProvider = controlClientProvider;
    }

    public Set<DatabaseHealthStatusDao> getDatabaseHealthStatusDaos() {
        return databaseHealthStatusDaos;
    }

    @Inject
    public void setDatabaseHealthStatusDaos(Set<DatabaseHealthStatusDao> databaseHealthStatusDaos) {
        this.databaseHealthStatusDaos = databaseHealthStatusDaos;
    }

    public RemoteInvokerRegistry getRemoteInvokerRegistry() {
        return remoteInvokerRegistry;
    }

    @Inject
    public void setRemoteInvokerRegistry(RemoteInvokerRegistry remoteInvokerRegistry) {
        this.remoteInvokerRegistry = remoteInvokerRegistry;
    }

    private static class HealthChecklist {

        private int performed = 0;

        private final HealthStatus healthStatus = new HealthStatus();

        private List<String> problems = new ArrayList<>();

        private Queue<Consumer<HealthChecklist>> operations = new LinkedList<>();

        public HealthChecklist with(final Consumer<HealthChecklist> op) {
            operations.add(op);
            return this;

        }

        public HealthStatus getHealthStatus() {
            return healthStatus;
        }

        public HealthStatus run() {

            if (operations.isEmpty()) throw new IllegalStateException("No checks performed.");

            Consumer<HealthChecklist> op;

            while ((op = operations.poll()) != null) {
                try {
                    op.accept(this);
                } catch (Exception ex) {
                    problems.add(ex.getMessage());
                    logger.error("Failed health check.", ex);
                } finally {
                    performed++;
                }
            }

            final double health = (1.0 - ((double) problems.size() / (double) performed)) * 100.0;

            healthStatus.setOverallHealth(health);
            healthStatus.setProblems(problems);
            healthStatus.setChecksFailed(problems.size());
            healthStatus.setChecksPerformed(performed);

            return healthStatus;

        }

    }

}
