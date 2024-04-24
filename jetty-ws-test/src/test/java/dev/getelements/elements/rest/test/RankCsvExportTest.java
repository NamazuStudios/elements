package dev.getelements.elements.rest.test;

import com.opencsv.CSVReader;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import dev.getelements.elements.Headers;
import dev.getelements.elements.dao.LeaderboardDao;
import dev.getelements.elements.dao.ScoreDao;
import dev.getelements.elements.model.leaderboard.Leaderboard;
import dev.getelements.elements.model.leaderboard.RankRow;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.TestUtils;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static dev.getelements.elements.model.leaderboard.Leaderboard.ScoreStrategyType.ACCUMULATE;
import static dev.getelements.elements.model.leaderboard.Leaderboard.TimeStrategyType.ALL_TIME;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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

    private List<Score> scores;

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

        scores = new ArrayList<>();

        for (int i = 0; i < 100; ++i) {

            final var client = clientContextProvider.get();
            client.createUser("test_csv")
                  .createProfile("TestCSV");

            final var score = new Score();
            score.setProfile(client.getDefaultProfile());
            score.setPointValue(10 * i);

            scores.add(score);
            scoreDao.createOrUpdateScore("test_csv_api", score);

        }
    }

    @Test
    public void testGetAnon() {

        final var response = client
                .target(apiRoot + "/rank/global/test_csv_api.csv")
                .request()
                .get();

        assertEquals(response.getStatus(), 403);

    }

    @Test
    public void testGetUser() {

        final var response = client
                .target(apiRoot + "/rank/global/test_csv_api.csv")
                .request()
                .header(Headers.SESSION_SECRET, user.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 403);

    }

    @Test
    public void testGetSuperUser() throws Exception {

        final var response = client
                .target(apiRoot + "/rank/global/test_csv_api.csv")
                .request()
                .header(Headers.SESSION_SECRET, superUser.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var entityStream = response.readEntity(InputStream.class);
        final var mappingStrategy = new HeaderColumnNameMappingStrategy<>();
        mappingStrategy.setType(RankRow.class);

        final var ranks = new ArrayList<RankRow>();

        try (var ioReader = new InputStreamReader(entityStream, StandardCharsets.UTF_8);
             var csvReader = new CSVReader(ioReader)) {

            mappingStrategy.captureHeader(csvReader);

            for (var line : csvReader) {
                final var rank = (RankRow) mappingStrategy.populateNewBean(line);
                ranks.add(rank);
            }

        }

        final var iterator = ranks.iterator();

        RankRow previous = null;

        while (iterator.hasNext()) {

            var rank = iterator.next();

            if (previous != null) {
                assertTrue(rank.getPosition() > previous.getPosition());
                assertTrue(rank.getPointValue() < previous.getPointValue());
            }

            previous = rank;

        }

    }

}
