package dev.getelements.elements.dao.mongo.migration;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;

import java.util.Date;

/**
 * Tracks that a {@link Migration} with a given id has been applied. The {@code _id} is the migration's id itself,
 * so inserting a record for an id already present fails with a duplicate-key error -- the mechanism
 * {@link MigrationRunner} relies on to claim a migration exactly once, even across concurrent/racing runners.
 */
@Entity(value = "schema_migrations", useDiscriminator = false)
public class SchemaMigrationRecord {

    @Id
    private String migrationId;

    @Property
    private Date appliedAt;

    public String getMigrationId() {
        return migrationId;
    }

    public void setMigrationId(String migrationId) {
        this.migrationId = migrationId;
    }

    public Date getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Date appliedAt) {
        this.appliedAt = appliedAt;
    }

}
