package dev.getelements.elements.dao.mongo.test;

import com.mongodb.client.MongoClient;
import dev.getelements.elements.dao.mongo.MongoElementEntityRegistrar;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigrationRunner;
import dev.getelements.elements.dao.mongo.model.mission.MongoMission;
import dev.morphia.config.MorphiaConfig;
import org.bson.Document;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.UUID;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.DATABASE_NAME;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * End-to-end regression for the registrar-built datastore (downstream report, rc-3): a MorphiaConfig
 * carrying ONLY an element's own packages must still produce a datastore whose mapper decodes the
 * platform's own model types — including legacy documents with embedded Morphia discriminators
 * ({@code steps[n]._t: "MongoStep"}), which is exactly the shape of pre-existing {@code mission} rows.
 */
@Guice(modules = IntegrationTestModule.class)
public class MongoElementEntityRegistrarPlatformDecodeTest {

    @Inject
    private MongoClient mongoClient;

    @Inject
    @Named(DATABASE_NAME)
    private String databaseName;

    @Inject
    private PreDatastoreMigrationRunner preDatastoreMigrationRunner;

    @Test
    public void elementOnlyConfigStillDecodesLegacyPlatformDocuments() {

        // A config as a downstream harness would supply it: the element's own packages only, none of
        // the platform's dao model tree, with the platform's discriminator settings (simple-name
        // discriminators are what legacy rows like steps[n]._t: "MongoStep" carry).

        final var elementOnlyConfig = MorphiaConfig.load()
                .applyIndexes(true)
                .enablePolymorphicQueries(true)
                .discriminator(dev.morphia.mapping.DiscriminatorFunction.className())
                .packages(List.of("com.example.cloud.documents"))
                .database(databaseName);

        // Build through the registrar's datastore hook, which applies the platform-model merge before
        // Morphia maps the config's packages.

        final var datastore = new MongoElementEntityRegistrar()
                .createDatastore(mongoClient, elementOnlyConfig, preDatastoreMigrationRunner);

        // Persist a raw document in the legacy shape: an embedded step carrying the Morphia
        // discriminator, as rows written by older versions have on disk.

        final var stepId = "legacy-step-" + UUID.randomUUID();

        final var rawStep = new Document("_t", "MongoStep")
                .append("displayName", stepId)
                .append("description", "legacy");

        final var missionId = new org.bson.types.ObjectId();

        datastore.getDatabase().getCollection("mission").insertOne(new Document()
                .append("_id", missionId)
                .append("name", "legacy-mission-" + UUID.randomUUID())
                .append("steps", List.of(rawStep))
        );

        // Decode through the mapped model: this is where rc-2 died with CodecConfigurationException.

        final var decoded = datastore.find(MongoMission.class)
                .filter(dev.morphia.query.filters.Filters.eq("_id", missionId))
                .first();

        assertNotNull(decoded, "the legacy mission row must decode");
        assertNotNull(decoded.getSteps());
        assertEquals(decoded.getSteps().size(), 1);
        assertEquals(decoded.getSteps().get(0).getDisplayName(), stepId);

    }

}
