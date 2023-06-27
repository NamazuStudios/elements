package dev.getelements.elements.dao;

import dev.getelements.elements.model.health.DatabaseHealthStatus;

public interface DatabaseHealthStatusDao {


    DatabaseHealthStatus checkDatabaseHealthStatus();

}
