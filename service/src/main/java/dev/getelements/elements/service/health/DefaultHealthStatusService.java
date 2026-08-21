package dev.getelements.elements.service.health;

import dev.getelements.elements.sdk.cluster.service.InstanceDiscoveryService;
import dev.getelements.elements.sdk.cluster.service.InstanceHostInfo;
import dev.getelements.elements.sdk.dao.DatabaseHealthStatusDao;
import dev.getelements.elements.sdk.model.health.DiscoveryHealthStatus;
import dev.getelements.elements.sdk.model.health.HealthStatus;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.health.HealthStatusService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Consumer;

import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

public class DefaultHealthStatusService implements HealthStatusService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultHealthStatusService.class);

    private MapperRegistry mapperRegistry;

    private InstanceDiscoveryService instanceDiscoveryService;

    private Set<DatabaseHealthStatusDao> databaseHealthStatusDaos;

    @Override
    public HealthStatus checkHealthStatus() {
        return new HealthChecklist()
                .with(this::checkDatabaseStatus)
                .with(this::checkDiscoveryStatus)
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
            .map(InstanceHostInfo::address)
            .collect(toList()));

        healthChecklist.getHealthStatus().setDiscoveryHealthStatus(discoveryHealthStatus);

    }

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public InstanceDiscoveryService getInstanceDiscoveryService() {
        return instanceDiscoveryService;
    }

    @Inject
    public void setInstanceDiscoveryService(InstanceDiscoveryService instanceDiscoveryService) {
        this.instanceDiscoveryService = instanceDiscoveryService;
    }

    public Set<DatabaseHealthStatusDao> getDatabaseHealthStatusDaos() {
        return databaseHealthStatusDaos;
    }

    @Inject
    public void setDatabaseHealthStatusDaos(Set<DatabaseHealthStatusDao> databaseHealthStatusDaos) {
        this.databaseHealthStatusDaos = databaseHealthStatusDaos;
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

            if (health < 100.0) {
                logger.warn("Below healthy threshold {}%", health);
                logger.warn("Encountered problems: [{}]", join(",", problems));
            }

            return healthStatus;

        }

    }

}
