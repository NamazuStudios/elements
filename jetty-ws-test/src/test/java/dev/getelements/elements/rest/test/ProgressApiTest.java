package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.mission.*;
import dev.getelements.elements.sdk.model.profile.Profile;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.*;

/**
 * Regression tests for POST /progress (createProgress) and GET /progress/{id}.
 *
 * Covers the bug where SuperUserProgressService.createProgress read profile from the
 * freshly-constructed Progress object instead of the request, always producing a null
 * profile and a 400 INVALID_DATA response.
 */
public class ProgressApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] { getInstance().getTestFixture(ProgressApiTest.class) };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUser;

    private Mission mission;

    private Progress createdProgress;

    @BeforeClass
    public void setUp() {
        superUser.createSuperuser("progress_api_superuser").createSession();
    }

    @BeforeClass(dependsOnMethods = "setUp")
    public void createMission() {
        final var request = new CreateMissionRequest();
        request.setName("progress_api_test_mission");
        request.setDisplayName("Progress API Test Mission");
        request.setDescription("Mission for ProgressApiTest regression coverage");

        final var step = new Step();
        step.setCount(5);
        step.setDisplayName("step_one");
        step.setDescription("step one");
        request.setSteps(java.util.List.of(step));

        final var response = client
                .target(apiRoot + "/mission")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "Pre-condition: mission creation must succeed");
        mission = response.readEntity(Mission.class);
        assertNotNull(mission.getId(), "mission id must be set");
    }

    // -- createProgress regression --

    @Test(dependsOnMethods = "createMission")
    public void testCreateProgressReturns200() {
        final var profile = superUser.getDefaultProfile();
        assertNotNull(profile, "superuser must have a default profile for this test");

        final var missionInfo = new ProgressMissionInfo();
        missionInfo.setId(mission.getId());

        final var request = new CreateProgressRequest();
        request.setProfile(profileRef(profile));
        request.setMission(missionInfo);

        final var response = client
                .target(apiRoot + "/progress")
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .post(Entity.entity(request, APPLICATION_JSON));

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(),
                "createProgress must return 200, not 400 — regression: profile was read from " +
                "the empty Progress instead of the request");

        createdProgress = response.readEntity(Progress.class);
        assertNotNull(createdProgress.getId(), "created progress must have an id");
    }

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testCreatedProgressHasCorrectProfile() {
        final var profile = superUser.getDefaultProfile();
        assertNotNull(createdProgress.getProfile(), "profile must not be null on the returned progress");
        assertEquals(createdProgress.getProfile().getId(), profile.getId(),
                "progress profile id must match the requested profile");
    }

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testCreatedProgressHasCorrectMission() {
        assertNotNull(createdProgress.getMission(), "mission must not be null on the returned progress");
        assertEquals(createdProgress.getMission().getId(), mission.getId(),
                "progress mission id must match the requested mission");
    }

    // -- getProgress --

    @Test(dependsOnMethods = "testCreateProgressReturns200")
    public void testGetProgressById() {
        final var response = client
                .target(apiRoot + "/progress/" + createdProgress.getId())
                .request(APPLICATION_JSON)
                .header(SESSION_SECRET, superUser.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        final var fetched = response.readEntity(Progress.class);
        assertEquals(fetched.getId(), createdProgress.getId());
    }

    // -- helpers --

    private static Profile profileRef(final Profile profile) {
        final var ref = new Profile();
        ref.setId(profile.getId());
        return ref;
    }

}
