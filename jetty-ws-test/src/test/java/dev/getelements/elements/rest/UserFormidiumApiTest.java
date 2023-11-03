package dev.getelements.elements.rest;

import dev.getelements.elements.model.formidium.FormidiumInvestor;
import dev.getelements.elements.rest.model.FormidiumInvestorPagination;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static java.util.stream.Stream.concat;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class UserFormidiumApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserFormidiumApiTest.class),
                TestUtils.getInstance().getUnixFSTest(UserFormidiumApiTest.class)
        };
    }

    private String apiUrl;

    private Client client;

    private ClientContext trudyClientContext;

    private List<ClientContext> explicitClientContexts;

    private List<ClientContext> implicitClientContexts;

    private Provider<ClientContext> clientContextProvider;

    private final Map<ClientContext, FormidiumInvestor> intermediates = new ConcurrentHashMap<>();

    @BeforeClass
    public void setupClients() {

        trudyClientContext = getClientContextProvider()
                .get()
                .createUser("formidium-trudy")
                .createSession();

        implicitClientContexts = IntStream.range(0, 10)
                .mapToObj(i -> getClientContextProvider()
                        .get()
                        .createUser("formidium")
                        .createSession()
                )
                .collect(toUnmodifiableList());

        explicitClientContexts = IntStream.range(0, 10)
                .mapToObj(i -> getClientContextProvider()
                        .get()
                        .createUser("formidium")
                        .createSession()
                )
                .collect(toUnmodifiableList());

    }

    @DataProvider
    public Object[][] allClientContexts() {
        return concat(implicitClientContexts.stream(), explicitClientContexts.stream())
                .map(c -> new Object[] {c})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] explicitClientContexts() {
        return explicitClientContexts.stream()
                .map(c -> new Object[] {c})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] implicitClientContexts() {
        return implicitClientContexts.stream()
                .map(c -> new Object[] {c})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] allClientContextsAndInvestors() {
        return concat(implicitClientContexts.stream(), explicitClientContexts.stream())
                .map(c -> new Object[] {c, intermediates.get(c)})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] allInvestors() {
        return intermediates.values().stream()
                .map(i -> new Object[] {i})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "implicitClientContexts", enabled = false)
    public void createFormidiumInvestorsImplicit(final ClientContext clientContext) {

        final var email = format("%s-%s@example.com", "formidium-test", UUID.randomUUID());

        final var multipart = new MultiPart()
                .bodyPart(new FormDataBodyPart("full_name", "Testy McTesterson"))
                .bodyPart(new FormDataBodyPart("investor_type", "IND"))
                .bodyPart(new FormDataBodyPart("email", email));

        final var response = client.target(getApiUrl() + "/kyc/formidium")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(entity(multipart, MULTIPART_FORM_DATA));

        assertEquals(response.getStatus(), 200);

        final var formidiumInvestor = response.readEntity(FormidiumInvestor.class);
        assertNotNull(formidiumInvestor.getId());
        assertNotNull(formidiumInvestor.getFormidiumInvestorId());
        assertEquals(clientContext.getUser(), formidiumInvestor.getUser());

        intermediates.put(clientContext, formidiumInvestor);

    }

    @Test(dataProvider = "explicitClientContexts", enabled = false)
    public void createFormidiumInvestorsExplicit(final ClientContext clientContext) {

        final var email = format("%s-%s@example.com", "formidium-test", UUID.randomUUID());

        final var multipart = new MultiPart()
                .bodyPart(new FormDataBodyPart("elements_user_id", clientContext.getUser().getId()))
                .bodyPart(new FormDataBodyPart("full_name", "Testy McTesterson"))
                .bodyPart(new FormDataBodyPart("investor_type", "IND"))
                .bodyPart(new FormDataBodyPart("email", email));

        final var response = client.target(getApiUrl() + "/kyc/formidium")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(entity(multipart, MULTIPART_FORM_DATA));

        assertEquals(200, response.getStatus());

        final var formidiumInvestor = response.readEntity(FormidiumInvestor.class);
        assertNotNull(formidiumInvestor.getId());
        assertNotNull(formidiumInvestor.getFormidiumInvestorId());
        assertEquals(clientContext.getUser(), formidiumInvestor.getUser());

        intermediates.put(clientContext, formidiumInvestor);

    }

    @Test(enabled = false, dataProvider = "allClientContexts", dependsOnMethods = {
            "createFormidiumInvestorsImplicit",
            "createFormidiumInvestorsExplicit"
    })
    public void testGetInvestors(final ClientContext clientContext) {

        final var response = client.target(getApiUrl() + "/kyc/formidium")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var formidiumInvestorPagination = response.readEntity(FormidiumInvestorPagination.class);
        assertNotNull(formidiumInvestorPagination);
        assertEquals(formidiumInvestorPagination.getObjects().size(), 1);

        final var investor = formidiumInvestorPagination.getObjects().get(0);
        assertEquals(investor.getUser(), clientContext.getUser());

    }

    @Test(enabled = false, dataProvider = "allClientContextsAndInvestors", dependsOnMethods = {
            "createFormidiumInvestorsImplicit",
            "createFormidiumInvestorsExplicit"
    })
    public void testGetSpecificInvestor(final ClientContext clientContext, final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var responseInvestor = response.readEntity(FormidiumInvestor.class);
        assertEquals(responseInvestor, formidiumInvestor);

    }

    @Test(enabled = false, dataProvider = "allInvestors", dependsOnMethods = {
            "createFormidiumInvestorsImplicit",
            "createFormidiumInvestorsExplicit"
    })
    public void testCrossUserGetInvestor(final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, trudyClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 404);

    }

    @Test(enabled = false, dependsOnMethods = {
            "createFormidiumInvestorsImplicit",
            "createFormidiumInvestorsExplicit"
    })
    public void testCrossUserGetInvestors() {

        final var response = client.target(getApiUrl() + "/kyc/formidium")
                .request()
                .header(SESSION_SECRET, trudyClientContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), 200);

        final var formidiumInvestorPagination = response.readEntity(FormidiumInvestorPagination.class);
        assertNotNull(formidiumInvestorPagination);
        assertEquals(formidiumInvestorPagination.getObjects().size(), 0);

    }

    @Test(enabled = false, dataProvider = "allClientContextsAndInvestors", dependsOnMethods = {
            "createFormidiumInvestorsImplicit",
            "createFormidiumInvestorsExplicit"
    })
    public void testUserCannotDelete(final ClientContext clientContext, final FormidiumInvestor formidiumInvestor) {

        final var response = client.target(getApiUrl() + "/kyc/formidium/" + formidiumInvestor.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .delete();

        assertEquals(response.getStatus(), 403);

    }

    public String getApiUrl() {
        return apiUrl;
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
