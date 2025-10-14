package dev.getelements.elements.rest.test;

import dev.getelements.elements.rest.test.model.MultiMatchPagination;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.match.MultiMatchStatus;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.model.match.MultiMatchStatus.OPEN;
import static jakarta.ws.rs.client.Entity.entity;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static java.lang.String.format;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class MultiMatchApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(MultiMatchApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext superUserClientContext;

    @Inject
    private ClientContext userClientContext;

    private MatchmakingApplicationConfiguration workingMatchmakingConfiguration;

    private List<MultiMatch> workingMatches = new ArrayList<>();

    @BeforeClass
    public void createSuperUser() {
        superUserClientContext
                .createSuperuser("multiMatchAdmin")
                .createSession();

        userClientContext.createUser("multiMatchPlayer1")
                         .createProfile("Player1")
                         .createSession();
    }

    @Test
    public void createMatchmakingConfiguration() {

        final var app = superUserClientContext.getApplication();
        final var matchmakingConfiguration = new MatchmakingApplicationConfiguration();
        matchmakingConfiguration.setName("multiMatchMatchmakingConfiguration");
        matchmakingConfiguration.setDescription("description");
        matchmakingConfiguration.setMaxProfiles(2);
        matchmakingConfiguration.setParent(app);

        final var request = client
                .target(apiRoot + "/application/" + app.getId() + "/configuration/matchmaking")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret());

        try(final var response = request.post(entity(matchmakingConfiguration, APPLICATION_JSON))) {

            assertEquals(200, response.getStatus());

            workingMatchmakingConfiguration = response.readEntity(MatchmakingApplicationConfiguration.class);
        }
    }

    @Test(groups = "create", dependsOnMethods = "createMatchmakingConfiguration", invocationCount = 10)
    public void testCreateMultiMatch() {

        final var multimatch = new MultiMatch();
        multimatch.setConfiguration(workingMatchmakingConfiguration);
        multimatch.setStatus(OPEN);
        multimatch.setMetadata(Map.of("code", "1234"));

        final var request = client
                .target(apiRoot + "/multi_match")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret());

        try(final var response = request.post(entity(multimatch, APPLICATION_JSON)))
        {
            assertEquals(200, response.getStatus());

            final var multiMatchResponse = response.readEntity(MultiMatch.class);

            assertNotNull(multiMatchResponse);
            assertNotNull(multiMatchResponse.getId());
            assertEquals(OPEN, multiMatchResponse.getStatus());
            assertEquals(multimatch.getMetadata(), multiMatchResponse.getMetadata());
            assertEquals(multimatch.getConfiguration(), multiMatchResponse.getConfiguration());

            workingMatches.add(multiMatchResponse);
        }

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateMetadata() {

        final var matchUpdate = new MultiMatch();
        final var workingMatch = workingMatches.getFirst();
        final var expiry = workingMatch.getExpiry() + 1000L;

        matchUpdate.setConfiguration(workingMatch.getConfiguration());
        matchUpdate.setMetadata(workingMatch.getMetadata());
        matchUpdate.setStatus(OPEN);
        matchUpdate.setExpiry(expiry);

        final var request = client
                .target(format("%s/multi_match/%s", apiRoot, workingMatch.getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret());

        try(final var response = request.put(entity(matchUpdate, APPLICATION_JSON))) {

            assertEquals(200, response.getStatus());

            final var multiMatchResponse = response.readEntity(MultiMatch.class);

            assertNotNull(multiMatchResponse);
            assertNotNull(multiMatchResponse.getId());
            assertEquals(matchUpdate.getStatus(), multiMatchResponse.getStatus());
            assertEquals(matchUpdate.getMetadata(), multiMatchResponse.getMetadata());
            assertEquals(matchUpdate.getExpiry(), multiMatchResponse.getExpiry());
            assertEquals(matchUpdate.getConfiguration(), multiMatchResponse.getConfiguration());

            workingMatches.set(0, multiMatchResponse);
        }
    }

    @Test(groups = "fetch")
    public void testGetBogusMatch() {

        final var response = client
                .target(format("%s/multi_match/asdf", apiRoot))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(404, response.getStatus());

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMatch() {

        final var response = client
                .target(format("%s/multi_match/%s", apiRoot, workingMatches.getFirst().getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(200, response.getStatus());

        final var match = response.readEntity(MultiMatch.class);
        assertEquals(workingMatches.getFirst(), match);

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMatches() {

        final PaginationWalker.WalkFunction<MultiMatch> walkFunction = (offset, count) -> {

            final var response = client
                    .target(format("%s/multi_match?offset=%d&count=%d",
                            apiRoot,
                            offset, count)
                    )
                    .request()
                    .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                    .get();

            assertEquals(200, response.getStatus());

            return response.readEntity(MultiMatchPagination.class);
        };

        final var specs = new PaginationWalker().toList(walkFunction);

        assertEquals(specs.size(), workingMatches.size());

        for (final MultiMatch spec : specs) {
            assertTrue(workingMatches.contains(spec));
        }
    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDeleteMatch() {

        final var request = client
                .target(format("%s/multi_match/%s", apiRoot, workingMatches.getFirst().getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret());

        try(final var response = request.delete()) {
            assertEquals(204, response.getStatus());
        }

    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testDoubleDelete() {

        final var request = client
                .target(format("%s/multi_match/%s", apiRoot, workingMatches.getFirst().getId()))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret());

        try(final var response = request.delete()) {
            assertEquals(404, response.getStatus());
        }

    }

    @Test(groups = "postDelete", dependsOnGroups = "delete")
    public void testDeleteAll() {

        final var request = client
                .target(format("%s/multi_match", apiRoot))
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret());

        try(final var response = request.delete()) {
            assertEquals(204, response.getStatus());
        }

    }

    @Test(groups = "postDeleteConfirmation", dependsOnGroups = "postDelete")
    public void testConfirmDeleteAll() {

        final var response = client
                .target(format("%s/multi_match?offset=%d&count=%d",
                        apiRoot,
                        0, 20)
                )
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(200, response.getStatus());

        final var pagination = response.readEntity(MultiMatchPagination.class);

        assertEquals(0, pagination.getTotal());
    }
}
