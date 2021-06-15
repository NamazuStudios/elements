package com.namazustudios.socialengine.service.health;

import com.namazustudios.socialengine.dao.DatabaseHealthStatusDao;
import com.namazustudios.socialengine.model.health.DatabaseHealthStatus;
import com.namazustudios.socialengine.model.health.HealthStatus;
import com.namazustudios.socialengine.model.health.InstanceHealthStatus;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.service.HealthStatusService;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class DefaultHealthStatusService implements HealthStatusService {

    private Mapper mapper;

    private Provider<ControlClient> controlClientProvider;

    private Set<DatabaseHealthStatusDao> databaseHealthStatusDaos;

    @Override
    public HealthStatus checkHealthStatus() {
        final var healthStatus = new HealthStatus();
        healthStatus.setDatabaseStatus(checkDatabaseStatus());
        healthStatus.setInstanceStatus(checkInstanceStatus());
        return healthStatus;
    }

    private List<DatabaseHealthStatus> checkDatabaseStatus() {
        return getDatabaseHealthStatusDaos()
            .stream()
            .map(DatabaseHealthStatusDao::checkDatabaseHealthStatus)
            .collect(toList());
    }

    private InstanceHealthStatus checkInstanceStatus() {
        try (var client = getControlClientProvider().get()) {
            final var instanceStatus = client.getInstanceStatus();
            return getMapper().map(instanceStatus, InstanceHealthStatus.class);
        }
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
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

}
