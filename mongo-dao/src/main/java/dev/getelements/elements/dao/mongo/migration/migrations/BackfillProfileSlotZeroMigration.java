package dev.getelements.elements.dao.mongo.migration.migrations;

import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import dev.getelements.elements.dao.mongo.migration.Migration;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.profile.MongoProfileSlot;
import dev.getelements.elements.dao.mongo.model.profile.MongoProfileSlotId;
import dev.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.morphia.query.filters.Filters.eq;

/**
 * Backfills a slot-{@code 0} {@link MongoProfileSlot} for every active {@link MongoProfile} that predates
 * per-application profile slots (introduced by issue #11), so pre-existing accounts are correctly counted
 * against {@code Application#getMaxProfiles()} and resolvable via {@code ProfileDao#findPrimaryProfile}.
 */
public class BackfillProfileSlotZeroMigration implements Migration {

    private static final Logger logger = LoggerFactory.getLogger(BackfillProfileSlotZeroMigration.class);

    public static final String ID = "20260819_01_backfill_profile_slot_zero";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Backfills a slot-0 MongoProfileSlot for every active Profile that predates per-application " +
                "profile slots (#11), so maxProfiles counting is correct for pre-existing accounts.";
    }

    @Override
    public void apply(final Datastore datastore) {

        final var query = datastore.find(MongoProfile.class).filter(eq("active", true));

        try (var iterator = query.iterator()) {
            while (iterator.hasNext()) {
                backfillSlotZero(datastore, iterator.next());
            }
        }

    }

    private void backfillSlotZero(final Datastore datastore, final MongoProfile profile) {

        final var slotId = new MongoProfileSlotId(
                profile.getUser().getObjectId(),
                profile.getApplication().getObjectId(),
                0);

        final var slot = new MongoProfileSlot();
        slot.setObjectId(slotId);
        slot.setProfileId(profile.getObjectId());

        try {
            datastore.insert(slot);
            logger.info("Backfilled profile slot 0 for profile {}", profile.getObjectId());
        } catch (MongoWriteException e) {
            if (e.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
                throw e;
            }
            logger.debug("Profile slot 0 already exists for user {} / application {}, skipping profile {}",
                    profile.getUser().getObjectId(), profile.getApplication().getObjectId(), profile.getObjectId());
        }

    }

}
