package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.model.ErrorResponse;
import com.namazustudios.socialengine.model.savedata.CreateSaveDataDocumentRequest;
import com.namazustudios.socialengine.model.savedata.SaveDataDocument;

import com.namazustudios.socialengine.model.savedata.UpdateSaveDataDocumentRequest;
import com.namazustudios.socialengine.rest.model.SaveDataDocumentPagination;
import com.namazustudios.socialengine.rt.util.Hex;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.exception.ErrorCode.*;
import static com.namazustudios.socialengine.rest.TestUtils.TEST_API_ROOT;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertEquals;

public class UserSaveDocumentApiTest {

    @Factory
    public static Object[] getTests() {
        return new Object[] {
            TestUtils.getInstance().getXodusTest(UserSaveDocumentApiTest.class),
            TestUtils.getInstance().getUnixFSTest(UserSaveDocumentApiTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext user0;

    @Inject
    private ClientContext user1;

    private final Map<DocumentKey, SaveDataDocument> localDocuments = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] getClientContexts() {
        return new Object[][] { {user0}, {user1} };
    }

    @DataProvider
    public Object[][] getClientContextsAndSlots() {
        return Stream.concat(
            range(0, 10).mapToObj(slot -> new Object[]{user0, slot}),
            range(0, 10).mapToObj(slot -> new Object[]{user1, slot})
        ).toArray(Object[][]::new);
    }

    @BeforeClass
    public void testSetup() {

        user0.createUser("save_data_user_0")
             .createProfile("save_data_profile_0")
             .createSession();

        user1.createUser("save_data_user_1")
             .createProfile("save_data_user_1")
             .createSession();

    }

    @Test(dataProvider = "getClientContextsAndSlots")
    public void testCreateUserSaveDocument(final ClientContext context, final int slot) {

        final var request = new CreateSaveDataDocumentRequest();
        request.setSlot(slot);
        request.setUserId(context.getUser().getId());
        request.setContents(randomUUID().toString());

        final var document = client
            .target(format("%s/save_data", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
            .readEntity(SaveDataDocument.class);

        assertNotNull(document.getId());
        assertNull(document.getProfile());
        assertNotNull(document.getVersion());
        assertEquals(slot, document.getSlot());
        assertEquals(request.getContents(), document.getContents());
        assertEquals(context.getUser().getId(), document.getUser().getId());

        saveLocalDocument(context.getUser().getId(), slot, document);

        var fetched = client
            .target(format("%s/save_data/%s", apiRoot, document.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get(SaveDataDocument.class);

        assertEquals(document, fetched);

        fetched = client
            .target(format("%s/save_data/user/%s/%s", apiRoot, document.getUser().getId(), slot))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get(SaveDataDocument.class);

        assertEquals(document, fetched);

    }

    @Test(dataProvider = "getClientContextsAndSlots")
    public void testCreateProfileSaveDocument(final ClientContext context, final int slot) {

        final var request = new CreateSaveDataDocumentRequest();
        request.setSlot(slot);
        request.setProfileId(context.getDefaultProfile().getId());
        request.setContents(randomUUID().toString());

        final var document = client
            .target(format("%s/save_data", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
            .readEntity(SaveDataDocument.class);

        assertNotNull(document.getId());
        assertNotNull(document.getVersion());
        assertEquals(slot, document.getSlot());
        assertEquals(request.getContents(), document.getContents());
        assertEquals(context.getUser().getId(), document.getUser().getId());
        assertEquals(context.getDefaultProfile().getId(), document.getProfile().getId());

        saveLocalDocument(context.getDefaultProfile().getId(), slot, document);

        var fetched = client
            .target(format("%s/save_data/%s", apiRoot, document.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get(SaveDataDocument.class);

        assertEquals(document, fetched);

        fetched = client
            .target(format("%s/save_data/profile/%s/%s", apiRoot, document.getProfile().getId(), slot))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get(SaveDataDocument.class);

        assertEquals(document, fetched);

    }

    @DataProvider
    public Object[][] getClientContextsAndSlotsForOpposingUsers() {
        return Stream.concat(
            range(0, 10).mapToObj(slot -> new Object[]{user0, user1, slot}),
            range(0, 10).mapToObj(slot -> new Object[]{user1, user0, slot})
        ).toArray(Object[][]::new);
    }

    @Test(dataProvider = "getClientContextsAndSlotsForOpposingUsers")
    public void testCreateUserSaveDocumentFailsAcrossUsers(final ClientContext context,
                                                           final ClientContext other,
                                                           final int slot) {

        final var request = new CreateSaveDataDocumentRequest();
        request.setSlot(slot);
        request.setUserId(other.getUser().getId());
        request.setContents(randomUUID().toString());

        final var response = client
                .target(format("%s/save_data", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(403, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(FORBIDDEN.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContextsAndSlotsForOpposingUsers")
    public void testCreateProfileSaveDocumentFailsAcrossUsers(final ClientContext context,
                                                              final ClientContext other,
                                                              final int slot) {

        final var request = new CreateSaveDataDocumentRequest();
        request.setSlot(slot);
        request.setProfileId(other.getDefaultProfile().getId());
        request.setContents(randomUUID().toString());

        final var response = client
                .target(format("%s/save_data", apiRoot))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(403, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(FORBIDDEN.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testCreateUserSaveDocument")
    public void testCheckedUpdateUserSaveDocument(final ClientContext context, final int slot) {

        final var existing = getLocalDocument(context.getUser().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(false);
        request.setVersion(existing.getVersion());
        request.setContents(randomUUID().toString());

        final var document = client
            .target(format("%s/save_data/%s", apiRoot, existing.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
            .readEntity(SaveDataDocument.class);

        assertNotNull(document.getId());
        assertNull(document.getProfile());
        assertNotNull(document.getVersion());
        assertEquals(slot, document.getSlot());
        assertEquals(request.getContents(), document.getContents());
        assertEquals(context.getUser().getId(), document.getUser().getId());

        saveLocalDocument(context.getUser().getId(), slot, document);

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testCreateProfileSaveDocument")
    public void testCheckedUpdateProfileSaveDocument(final ClientContext context, final int slot) {

        final var existing = getLocalDocument(context.getDefaultProfile().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(false);
        request.setVersion(existing.getVersion());
        request.setContents(randomUUID().toString());

        final var document = client
            .target(format("%s/save_data/%s", apiRoot, existing.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
            .readEntity(SaveDataDocument.class);

        assertNotNull(document.getId());
        assertNotNull(document.getVersion());
        assertEquals(slot, document.getSlot());
        assertEquals(request.getContents(), document.getContents());
        assertEquals(context.getUser().getId(), document.getUser().getId());
        assertEquals(context.getDefaultProfile().getId(), document.getProfile().getId());

        saveLocalDocument(context.getDefaultProfile().getId(), slot, document);

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testCreateUserSaveDocument")
    public void testCheckedUpdateUserSaveDocumentFail(final ClientContext context, final int slot) {

        final var bytes = new byte[32];
        ThreadLocalRandom.current().nextBytes(bytes);

        final var existing = getLocalDocument(context.getUser().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(false);
        request.setVersion(Hex.encode(bytes));
        request.setContents(randomUUID().toString());

        final var response = client
                .target(format("%s/save_data/%s", apiRoot, existing.getId()))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(409, response.getStatus());

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(CONFLICT.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testCreateProfileSaveDocument")
    public void testCheckedUpdateProfileSaveDocumentFail(final ClientContext context, final int slot) {

        final var bytes = new byte[32];
        ThreadLocalRandom.current().nextBytes(bytes);

        final var existing = getLocalDocument(context.getDefaultProfile().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(false);
        request.setVersion(Hex.encode(bytes));
        request.setContents(randomUUID().toString());

        final var response = client
                .target(format("%s/save_data/%s", apiRoot, existing.getId()))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(CONFLICT.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testCheckedUpdateUserSaveDocument")
    public void testForcedUpdateUserSaveDocument(final ClientContext context, final int slot) {

        final var existing = getLocalDocument(context.getUser().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(true);
        request.setContents(randomUUID().toString());

        final var document = client
                .target(format("%s/save_data/%s", apiRoot, existing.getId()))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(SaveDataDocument.class);

        assertNotNull(document.getId());
        assertNull(document.getProfile());
        assertNotNull(document.getVersion());
        assertEquals(slot, document.getSlot());
        assertEquals(request.getContents(), document.getContents());
        assertEquals(context.getUser().getId(), document.getUser().getId());

        saveLocalDocument(context.getUser().getId(), slot, document);

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testCheckedUpdateProfileSaveDocument")
    public void testForcedUpdateProfileSaveDocument(final ClientContext context, final int slot) {

        final var existing = getLocalDocument(context.getDefaultProfile().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(true);
        request.setContents(randomUUID().toString());

        final var document = client
                .target(format("%s/save_data/%s", apiRoot, existing.getId()))
                .request()
                .header("Authorization", format("Bearer %s", context.getSessionSecret()))
                .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(SaveDataDocument.class);

        assertNotNull(document.getId());
        assertNotNull(document.getVersion());
        assertEquals(slot, document.getSlot());
        assertEquals(request.getContents(), document.getContents());
        assertEquals(context.getUser().getId(), document.getUser().getId());
        assertEquals(context.getDefaultProfile().getId(), document.getProfile().getId());

        saveLocalDocument(context.getDefaultProfile().getId(), slot, document);

    }

    @Test(dataProvider = "getClientContextsAndSlotsForOpposingUsers",
          dependsOnMethods = "testCreateUserSaveDocument")
    public void testUpdateUserSaveDocumentFailsAcrossUsers(final ClientContext context,
                                                           final ClientContext other,
                                                           final int slot) {

        final var existing = getLocalDocument(other.getUser().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(false);
        request.setVersion(existing.getVersion());
        request.setContents(randomUUID().toString());

        final var response = client
            .target(format("%s/save_data/%s", apiRoot, existing.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(404, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(NOT_FOUND.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContextsAndSlotsForOpposingUsers",
          dependsOnMethods = "testCreateProfileSaveDocument")
    public void testUpdateProfileSaveDocumentFailsAcrossUsers(final ClientContext context,
                                                              final ClientContext other,
                                                              final int slot) {

        final var existing = getLocalDocument(other.getDefaultProfile().getId(), slot);

        final var request = new UpdateSaveDataDocumentRequest();
        request.setForce(false);
        request.setVersion(existing.getVersion());
        request.setContents(randomUUID().toString());

        final var response = client
            .target(format("%s/save_data/%s", apiRoot, existing.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .put(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));

        final int status = response.getStatus();
        assertEquals(404, status);

        final var error = response.readEntity(ErrorResponse.class);
        assertEquals(NOT_FOUND.toString(), error.getCode());

    }

    @Test(dataProvider = "getClientContexts", dependsOnMethods = {
            "testCreateUserSaveDocument",
            "testCreateProfileSaveDocument"
        }
    )
    public void testGetDocuments(final ClientContext context) {

        final var pagination = client
            .target(format("%s/save_data?offset=0&count=100", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get(SaveDataDocumentPagination.class);

        assertEquals(20, pagination.getTotal());

        for (var doc : pagination.getObjects()) {
            assertEquals(context.getUser().getId(), doc.getUser().getId());
        }

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testGetDocuments")
    public void testDeleteUserDocument(final ClientContext context, final int slot) {

        final var removed = removeLocalDocument(context.getUser().getId(), slot);

        final var deleteResponse = client
            .target(format("%s/save_data/%s", apiRoot, removed.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .delete();

        assertEquals(204, deleteResponse.getStatus());

        final var getResponse = client
            .target(format("%s/save_data/%s", apiRoot, removed.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get();

        assertEquals(404, getResponse.getStatus());

    }

    @Test(dataProvider = "getClientContextsAndSlots", dependsOnMethods = "testGetDocuments")
    public void testDeleteProfileDocument(final ClientContext context, final int slot) {

        final var removed = removeLocalDocument(context.getDefaultProfile().getId(), slot);

        final var deleteResponse = client
            .target(format("%s/save_data/%s", apiRoot, removed.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .delete();

        assertEquals(204, deleteResponse.getStatus());

        final var getResponse = client
            .target(format("%s/save_data/%s", apiRoot, removed.getId()))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get();

        assertEquals(404, getResponse.getStatus());

    }

    @Test(dataProvider = "getClientContexts", dependsOnMethods = {
        "testDeleteUserDocument",
        "testDeleteProfileDocument"
    })
    public void testAllAreDeleted(final ClientContext context) {

        final var pagination = client
            .target(format("%s/save_data?offset=0&count=100", apiRoot))
            .request()
            .header("Authorization", format("Bearer %s", context.getSessionSecret()))
            .get(SaveDataDocumentPagination.class);

        assertEquals(0, pagination.getTotal());

    }
    
    private SaveDataDocument getLocalDocument(final String context, final int slot) {
        return localDocuments.get(new DocumentKey(slot, context));
    }

    private SaveDataDocument saveLocalDocument(final String context, final int slot,
                                               final SaveDataDocument document) {
        return localDocuments.put(new DocumentKey(slot, context), document);
    }

    private SaveDataDocument removeLocalDocument(final String context, final int slot) {
        return localDocuments.remove(new DocumentKey(slot, context));
    }
    
    private class DocumentKey {

        private final int slot;

        private final String context;

        public DocumentKey(final int slot, final String context) {
            this.slot = slot;
            this.context = context;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DocumentKey that = (DocumentKey) o;
            return slot == that.slot && Objects.equals(context, that.context);
        }

        @Override
        public int hashCode() {
            return Objects.hash(slot, context);
        }

    }

}
