package dev.getelements.elements.rest;

import dev.getelements.elements.model.formidium.FormidiumInvestor;
import dev.getelements.elements.rest.model.FormidiumInvestorPagination;
import dev.getelements.elements.util.PaginationWalker;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.testng.AssertJUnit.*;

public class SuperUserFormidiumApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(SuperUserFormidiumApiTest.class),
                TestUtils.getInstance().getUnixFSTest(SuperUserFormidiumApiTest.class)
        };
    }

    private String apiUrl;

    private Client client;

    private ClientContext superUserClientContext;

    private List<ClientContext> userClientContexts;

    private Provider<ClientContext> clientContextProvider;

    private final Map<ClientContext, FormidiumInvestor> intermediates = new ConcurrentHashMap<>();

    public String getApiUrl() {
        return apiUrl;
    }

    @BeforeClass
    public void setupClients() {

        superUserClientContext = getClientContextProvider()
                .get()
                .createSuperuser("formidium-admin")
                .createSession();

        userClientContexts = IntStream.range(0, 25)
                .mapToObj(i -> getClientContextProvider()
                        .get()
                        .createUser("formidium")
                        .createSession()
                )
                .collect(toUnmodifiableList());

    }

    @DataProvider
    public Object[][] userClientContexts() {
        return userClientContexts
                .stream()
                .map(c -> new Object[] {c})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] allInvestors() {
        return intermediates.values().stream()
                .map(i -> new Object[] {i})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "userClientContexts", enabled = false)
    public void createFormidiumInvestors(final ClientContext clientContext) {

        final var email = format("%s-%s@example.com", "formidium-test", UUID.randomUUID());

        final var multipart = new MultiPart()
                .bodyPart(new FormDataBodyPart("elements_user_id", clientContext.getUser().getId()))
                .bodyPart(new FormDataBodyPart("full_name", "Testy McTesterson"))
                .bodyPart(new FormDataBodyPart("investor_type", "IND"))
                .bodyPart(new FormDataBodyPart("email", email));

        final var response = client.target(getApiUrl() + "/kyc/formidium")
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .post(entity(multipart, MULTIPART_FORM_DATA));

        assertEquals(response.getStatus(), 200);

        final var formidiumInvestor = response.readEntity(FormidiumInvestor.class);
        assertNotNull(formidiumInvestor.getId());
        assertNotNull(formidiumInvestor.getFormidiumInvestorId());
        assertEquals(clientContext.getUser(), formidiumInvestor.getUser());

        intermediates.put(clientContext, formidiumInvestor);

    }

    @Test(dependsOnMethods = "createFormidiumInvestors", enabled = false)
    public void getFormidiumInvestors() {

        final var investors = new PaginationWalker().toList((offset, count) -> {

            final var response = client.target(getApiUrl() + "/kyc/formidium")
                    .queryParam("count", count)
                    .queryParam("offset", offset)
                    .request()
                    .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                    .get();

            assertEquals(response.getStatus(), 200);

            return response.readEntity(FormidiumInvestorPagination.class);

        });

        assertTrue(investors.containsAll(intermediates.values()));

    }

    @Test(dataProvider = "allInvestors", dependsOnMethods = "createFormidiumInvestors", enabled = false)
    public void getFormidiumInvestor(final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var responseInvestor = response.readEntity(FormidiumInvestor.class);
        assertEquals(formidiumInvestor, responseInvestor);

    }

    @Test(dataProvider = "allInvestors", dependsOnMethods = {"getFormidiumInvestors", "getFormidiumInvestor"}, enabled = false)
    public void deleteFormidiumInvestor(final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 204);

    }

    @Test(dataProvider = "allInvestors", dependsOnMethods = {"deleteFormidiumInvestor"}, enabled = false)
    public void doubleDeleteFormidiumInvestor(final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 404);

    }

    @Test(dataProvider = "allInvestors", dependsOnMethods = {"deleteFormidiumInvestor"}, enabled = false)
    public void deletedNotFound(final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, superUserClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }

    @Inject
    public void setApiUrl(@Named(TEST_API_ROOT) final String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public Provider<ClientContext> getClientContextProvider() {
        return clientContextProvider;
    }

    @Inject
    public void setClientContextProvider(Provider<ClientContext> clientContextProvider) {
        this.clientContextProvider = clientContextProvider;
    }

}
