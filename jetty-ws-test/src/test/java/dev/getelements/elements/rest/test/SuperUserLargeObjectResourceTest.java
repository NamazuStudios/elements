package dev.getelements.elements.rest.test;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.rest.test.ClientContext;
import dev.getelements.elements.rest.test.LargeObjectRequestFactory;
import dev.getelements.elements.rest.test.TestUtils;
import dev.getelements.elements.rt.http.HttpStatus;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static dev.getelements.elements.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.LargeObjectRequestFactory.DEFAULT_MIME_TYPE;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.*;

public class SuperUserLargeObjectResourceTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getXodusTest(SuperUserLargeObjectResourceTest.class),
                TestUtils.getInstance().getUnixFSTest(SuperUserLargeObjectResourceTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private LargeObjectRequestFactory requestFactory;

    @Inject
    private ClientContext clientContext;

    @Inject
    private ClientContext notSUContext;

    @BeforeClass
    private void setUp() {
        clientContext.createSuperuser("uploadingSuperUser").createSession();
        notSUContext.createUser("otherUser").createSession();
    }

    @Test()
    public void shouldCreateFullAccessLargeObject() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        final LargeObject largeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        assertNotNull(largeObject);
        assertNotNull(largeObject.getPath());
        assertEquals(largeObject.getMimeType(), DEFAULT_MIME_TYPE);
        assertTrue(largeObject.getAccessPermissions().getRead().isWildcard());
        assertTrue(largeObject.getAccessPermissions().getWrite().isWildcard());
        assertTrue(largeObject.getAccessPermissions().getDelete().isWildcard());
    }

    @Test
    public void shouldCreateReadOnlyObject() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithAccess(true, false, false);

        final LargeObject largeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        assertTrue(largeObject.getAccessPermissions().getRead().isWildcard());
        assertFalse(largeObject.getAccessPermissions().getWrite().isWildcard());
        assertFalse(largeObject.getAccessPermissions().getDelete().isWildcard());
    }

    @Test()
    public void shouldGetLargeObject() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject foundlargeObject = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .readEntity(LargeObject.class);

        assertNotNull(foundlargeObject);
        assertEquals(foundlargeObject.getMimeType(), DEFAULT_MIME_TYPE);
        assertTrue(foundlargeObject.getAccessPermissions().getRead().isWildcard());
        assertTrue(foundlargeObject.getAccessPermissions().getWrite().isWildcard());
        assertTrue(foundlargeObject.getAccessPermissions().getDelete().isWildcard());
    }

    @Test()
    public void shouldDeleteLargeObject() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject foundlargeObject = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .readEntity(LargeObject.class);

        //delete
        LargeObject deletedlargeObject = client.target(apiRoot + "/large_object/" + foundlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .delete()
                .readEntity(LargeObject.class);

        final LargeObject foundDeletedlargeObject = client
                .target(apiRoot + "/large_object/" + deletedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .readEntity(LargeObject.class);

        //deleted object data is null
         assertNull(foundDeletedlargeObject.getId());
    }

    @Test()
    public void shouldUpdateLargeObject() {

        CreateLargeObjectRequest createRequest = requestFactory.createRequestWithAccess(true, true, false);
        UpdateLargeObjectRequest updateRequest = requestFactory.updateLargeObjectRequest(false, false, true);
        updateRequest.setMimeType("changedMime");

        final LargeObject createdLargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject updatedLargeObject = client
                .target(apiRoot + "/large_object/" + createdLargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(updateRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        assertNotNull(updatedLargeObject);
        assertEquals(updatedLargeObject.getMimeType(), "changedMime");
        assertFalse(updatedLargeObject.getAccessPermissions().getRead().isWildcard());
        assertFalse(updatedLargeObject.getAccessPermissions().getWrite().isWildcard());
        assertTrue(updatedLargeObject.getAccessPermissions().getDelete().isWildcard());

    }

    @Test()
    public void shouldCreateLargeObjectFromUrl() {

        CreateLargeObjectRequest createRequest = requestFactory.createRequestFromUrlWithAccess(true, true, true);

        final LargeObject largeObject = client
                .target(apiRoot + "/large_object/from_url")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject foundlargeObject = client
                .target(apiRoot + "/large_object/" + largeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .readEntity(LargeObject.class);

        assertNotNull(foundlargeObject);
        assertEquals(foundlargeObject.getMimeType(), createRequest.getMimeType());
        assertNotNull(foundlargeObject.getPath());
        assertTrue(foundlargeObject.getAccessPermissions().getRead().isWildcard());
        assertTrue(foundlargeObject.getAccessPermissions().getWrite().isWildcard());
        assertTrue(foundlargeObject.getAccessPermissions().getDelete().isWildcard());
    }

    @Test()
    public void shouldGetLargeObjectForSpecificUserAccess() {
        CreateLargeObjectRequest request = requestFactory
                .createRequestWithUserAccess(asList(clientContext.getUser().getId()), emptyList(), emptyList());

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject foundlargeObject = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .readEntity(LargeObject.class);

        assertNotNull(foundlargeObject);
        assertEquals(foundlargeObject.getMimeType(), DEFAULT_MIME_TYPE);
        assertFalse(foundlargeObject.getAccessPermissions().getRead().isWildcard());
    }

    @Test()
    public void shouldNotCreateWithoutProperUserPrivileges() {
        CreateLargeObjectRequest request = requestFactory
                .createRequestWithUserAccess(asList("randomOtherID"), emptyList(), emptyList());

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final int status = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, notSUContext.getSessionSecret())
                .get().getStatus();

        assertNull(savedlargeObject.getId());   //object was not created
        assertEquals(status, HttpStatus.NOT_FOUND.getCode());
    }

    @Test()
    public void shouldNotGetLargeObjectForWrongLoggedUser() {
        CreateLargeObjectRequest request = requestFactory
                .createRequestWithUserAccess(asList(clientContext.getUser().getId()), emptyList(), emptyList());

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final int status = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, notSUContext.getSessionSecret())
                .get().getStatus();

        assertEquals(status, HttpStatus.FORBIDDEN.getCode());
    }
}