package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
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
import java.util.UUID;
import java.util.stream.IntStream;

import static com.namazustudios.socialengine.Headers.SESSION_SECRET;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toUnmodifiableList;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

public class UserFormidiumApiTest {

    private static final Logger logger = LoggerFactory.getLogger(UserFormidiumApiTest.class);

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(UserFormidiumApiTest.class),
                TestUtils.getInstance().getUnixFSTest(UserFormidiumApiTest.class)
        };
    }

    private String apiUrl;

    private Client client;

    private List<ClientContext> clientContexts;

    private Provider<ClientContext> clientContextProvider;

    @BeforeClass
    public void setupClients() {
        clientContexts = IntStream.range(0, 10)
                .mapToObj(i -> getClientContextProvider()
                        .get()
                        .createUser("formidium")
                        .createSession()
                )
                .collect(toUnmodifiableList());
    }

    @DataProvider
    public Object[][] allClientContexts() {
        return clientContexts
                .stream()
                .map(c -> new Object[] {c})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "allClientContexts")
    public void createFormidiumInvestors(final ClientContext clientContext) {

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
