package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.goods.CreateItemRequest;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.mission.CreateMissionRequest;
import dev.getelements.elements.sdk.model.mission.CreateProgressRequest;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressMissionInfo;
import dev.getelements.elements.sdk.model.mission.Step;
import dev.getelements.elements.sdk.model.reward.Reward;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import java.util.List;

import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static java.lang.String.format;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Regression test for issue NamazuStudios/elements#2.
 *
 * <p>{@code SuperUserProgressService.createProgress} set the profile from the freshly
 * constructed (empty) {@link Progress} instead of the incoming {@link CreateProgressRequest},
 * so {@code POST /api/rest/progress} always 400s with {@code "profile - must not be null"}
 * even when a valid profile is supplied. Present in 3.7.28 and 3.8.4.
 *
 * <p>Drives the full REST path (superuser session → {@code POST /item} → {@code POST /mission}
 * → {@code POST /progress}) and asserts the progress is created (HTTP 200, not 400) with the
 * supplied profile persisted on the returned {@link Progress}.
 */
public class CreateProgressApiTest {

    private static final String SUFFIX = Long.toString(System.currentTimeMillis());

    @Factory
    public static Object[] getTests() {
        return new Object[] {
                getInstance().getTestFixture(CreateProgressApiTest.class)
        };
    }

    @Inject
    private ApiRoot apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user;

    @Inject
    private ClientContext superUser;

    private Item rewardItem;

    private Mission mission;

    @BeforeClass
    public void createUsers() {
        user.createUser("cpa_user_" + SUFFIX)
                .createProfile("cpa_profile_" + SUFFIX)
                .createSessionWithDefaultProfile();

        superUser.createSuperuser("cpa_admin_" + SUFFIX)
                .createSession();
    }

    @BeforeClass(dependsOnMethods = "createUsers")
    public void createRewardItem() {
        final var request = new CreateItemRequest();
        request.setName("cpa_reward_item_" + SUFFIX);
        request.setDisplayName("cpa reward item");
        request.setDescription("cpa reward item");
        request.setCategory(ItemCategory.FUNGIBLE);

        final var response = client
                .target(apiRoot + "/item")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200, "POST /item should succeed");
        rewardItem = response.readEntity(Item.class);
    }

    @BeforeClass(dependsOnMethods = "createRewardItem")
    public void createMission() {
        final var reward = new Reward();
        reward.setQuantity(1);
        reward.setItem(rewardItem);

        final var step = new Step();
        step.setCount(1);
        step.setDescription("cpa step");
        step.setDisplayName("cpa step");
        step.setRewards(List.of(reward));

        final var createMission = new CreateMissionRequest();
        createMission.setName("cpa_mission_" + SUFFIX);
        createMission.setDisplayName("cpa mission");
        createMission.setDescription("cpa mission");
        createMission.setSteps(List.of(step));

        final var response = client
                .target(apiRoot + "/mission")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(createMission, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200, "POST /mission should succeed");
        mission = response.readEntity(Mission.class);
    }

    /**
     * The regression: pre-fix this returned 400 "profile - must not be null" because
     * {@code SuperUserProgressService.createProgress} read the profile off the empty
     * {@link Progress} rather than the {@link CreateProgressRequest}. Post-fix the
     * profile is persisted and echoed back on the created {@link Progress}.
     */
    @Test
    public void testCreateProgressPersistsProfile() {

        final var request = new CreateProgressRequest();
        request.setProfile(user.getDefaultProfile());
        request.setMission(newProgressMissionInfo(mission));

        final var response = client
                .target(apiRoot + "/progress")
                .request()
                .header("Authorization", format("Bearer %s", superUser.getSessionSecret()))
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), 200,
                "POST /progress must not 400 with 'profile - must not be null'");
        final var progress = response.readEntity(Progress.class);
        assertNotNull(progress, "POST /progress should return the created Progress");
        assertNotNull(progress.getProfile(), "created Progress must persist the supplied profile");
        assertEquals(progress.getProfile().getId(), user.getDefaultProfile().getId(),
                "created Progress must reference the supplied profile id");
    }

    private ProgressMissionInfo newProgressMissionInfo(final Mission mission) {
        final var info = new ProgressMissionInfo();
        info.setId(mission.getId());
        info.setName(mission.getName());
        info.setDisplayName(mission.getDisplayName());
        info.setDescription(mission.getDescription());
        info.setSteps(mission.getSteps());
        info.setTags(mission.getTags());
        info.setMetadata(mission.getMetadata());
        return info;
    }

}
