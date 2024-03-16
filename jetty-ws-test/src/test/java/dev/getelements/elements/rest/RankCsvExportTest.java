package dev.getelements.elements.rest;

import dev.getelements.elements.dao.CustomAuthUserDao;
import dev.getelements.elements.dao.LeaderboardDao;
import dev.getelements.elements.dao.ScoreDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.Score;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static dev.getelements.elements.model.leaderboard.Leaderboard.ScoreStrategyType.ACCUMULATE;
import static dev.getelements.elements.model.leaderboard.Leaderboard.TimeStrategyType.ALL_TIME;
import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.testng.Assert.assertEquals;

public class RankCsvExportTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(RankCsvExportTest.class),
                TestUtils.getInstance().getUnixFSTest(RankCsvExportTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user;

    @Inject
    private ClientContext superUser;

    @Inject
    private ScoreDao scoreDao;

    @Inject
    private LeaderboardDao leaderboardDao;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    @BeforeClass
    public void setupUsers() {
        user.createUser("test_csv_scores").createSession();
        superUser.createSuperuser("test_csv_scores").createSession();
    }

    @BeforeClass
    public void setupLeaderboard() {
        final var leaderboard = new Leaderboard();
        leaderboard.setName("test_csv_api");
        leaderboard.setScoreUnits("Points");
        leaderboard.setTitle("Test CSV API");
        leaderboard.setTimeStrategyType(ALL_TIME);
        leaderboard.setScoreStrategyType(ACCUMULATE);
        leaderboardDao.createLeaderboard(leaderboard);
    }

    @BeforeClass(dependsOnMethods = "setupLeaderboard")
    public void setupScores() {
        for (int i = 0; i < 100; ++i) {

            final var client = clientContextProvider.get();
            client.createUser("test_csv")
                  .createProfile("TestCSV");

            final var score = new Score();
            score.setProfile(client.getDefaultProfile());
            score.setPointValue(10 * i);

            scoreDao.createOrUpdateScore("test_csv_api", score);

        }
    }

    @Test
    public void testGetAnon() {
        // global/{leaderboardNameOrId}.csv

        final var response = client
                .target(apiRoot + "/rank/global/test_csv_api.csv")
                .request()
                .get();

        assertEquals(response.getStatus(), 403);


    }

    @Test
    public void testGetUser() {
    }

    @Test
    public void testGetSuperUser() {
    }

}
