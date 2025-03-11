package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.health.DatabaseHealthStatus;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

@ElementServiceExport
public interface DatabaseHealthStatusDao {


    DatabaseHealthStatus checkDatabaseHealthStatus();

}
