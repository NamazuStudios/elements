package dev.getelements.elements.dao.mongo.health;

import com.mongodb.client.MongoDatabase;
import dev.getelements.elements.sdk.dao.DatabaseHealthStatusDao;
import dev.getelements.elements.sdk.model.health.DatabaseHealthStatus;
import org.bson.Document;

import jakarta.inject.Inject;

import static java.lang.String.format;

public class MongoDatabaseHealthStatusDao implements DatabaseHealthStatusDao {

    private MongoDatabase mongoDatabase;

    @Override
    public DatabaseHealthStatus checkDatabaseHealthStatus() {

        final var databaseName = getDatabaseName();
        final var databaseMetadata = getDatabaseMetadata();

        final var status = new DatabaseHealthStatus();
        status.setName(databaseName);
        status.setMetadata(databaseMetadata);

        return status;
    }

    final String getDatabaseName() {

        final var command = new Document().append("buildInfo", 1);
        final var commandResult = getMongoDatabase().runCommand(command);

        return format(
            "mongodb %s (%s)",
            commandResult.getString("version"),
            commandResult.getString("gitVersion")
        );

    }

    private String getDatabaseMetadata() {
        final var command = new Document().append("serverStatus", 1);
        final var commandResult = getMongoDatabase().runCommand(command);
        return commandResult.toJson();
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    @Inject
    public void setMongoDatabase(MongoDatabase mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }

}
