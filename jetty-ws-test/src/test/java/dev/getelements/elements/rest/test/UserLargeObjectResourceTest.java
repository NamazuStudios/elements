package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.io.InputStream;

import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.rest.test.LargeObjectRequestFactory.DEFAULT_MIME_TYPE;
import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.rest.test.TestUtils.getInstance;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.testng.Assert.*;

public class UserLargeObjectResourceTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                getInstance().getTestFixture(UserLargeObjectResourceTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private ClientContext clientContext;

    @Inject
    private ClientContext superuserClientContext;

    @Inject
    private LargeObjectRequestFactory requestFactory;

    @BeforeClass
    private void setUp() {
        clientContext.createUser("uploadingUser").createProfile("test").createSession();
        superuserClientContext.createSuperuser("uploadingSuperuser").createSession();
    }

    @Test()
    public void shouldNotCreateLargeObject() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        final int status = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .getStatus();

        assertEquals(status, 403);
    }

    @Test()
    public void shouldGetLargeObject() {

        CreateLargeObjectRequest request = requestFactory.createRequestWithFullAccess();

        //superuser creates lo
        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
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
    public void shouldNotGetLargeObjectWithoutAccess() {
        CreateLargeObjectRequest request = requestFactory.createRequestWithAccess(false, true, true);

        //superuser creates lo
        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final int status = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .getStatus();

        assertEquals(status, 403);
    }

    @Test()
    public void shouldGetLargeObjectForSpecificUserAccess() {
        CreateLargeObjectRequest request = requestFactory
                .createRequestWithUserAccess(asList(clientContext.getUser().getId()), emptyList(), emptyList());

        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
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
    public void shouldAllowToWriteLargeObjectConent() {
        CreateLargeObjectRequest createRequest = requestFactory.createRequestWithFullAccess();
        final InputStream loStream = UserLargeObjectResourceTest.class.getResourceAsStream("/testLO.txt");

        //superuser creates lo
        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final FormDataMultiPart multipart = new FormDataMultiPart();
        multipart.field("file", loStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        final LargeObject objectWithContent = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId() + "/content")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(multipart, multipart.getMediaType()))
                .readEntity(LargeObject.class);

        assertNotNull(objectWithContent);
        assertEquals(objectWithContent.getMimeType(), DEFAULT_MIME_TYPE);
        assertTrue(objectWithContent.getAccessPermissions().getWrite().isWildcard());
    }

    @Test()
    public void shouldNotAllowToWriteLargeObjectConent() {
        CreateLargeObjectRequest createRequest = requestFactory.createRequestWithAccess(true, false, true);
        final InputStream loStream = UserLargeObjectResourceTest.class.getResourceAsStream("/testLO.txt");

        //superuser creates lo
        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final FormDataMultiPart multipart = new FormDataMultiPart();
        multipart.field("file", loStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);

        final int status = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId() + "/content")
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(multipart, multipart.getMediaType()))
                .getStatus();

        assertEquals(status, 403);
    }

    @Test()
    public void shouldAllowToWriteAndDelete() {
        CreateLargeObjectRequest createRequest = requestFactory.createRequestWithFullAccess();
        final InputStream loStream = UserLargeObjectResourceTest.class.getResourceAsStream("/testLO.txt");

        //superuser creates lo
        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject objectWithContent = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(loStream, MediaType.APPLICATION_OCTET_STREAM))
                .readEntity(LargeObject.class);

        final LargeObject deletedlargeObject = client
                .target(apiRoot + "/large_object/" + objectWithContent.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .delete()
                .readEntity(LargeObject.class);

        final LargeObject foundDeletedLargeObject = client
                .target(apiRoot + "/large_object/" + deletedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .readEntity(LargeObject.class);

        assertNull(foundDeletedLargeObject.getId());
    }

    @Test()
    public void shouldNotAllowToDelete() {
        CreateLargeObjectRequest createRequest = requestFactory.createRequestWithAccess(true, true, false);
        final InputStream loStream = UserLargeObjectResourceTest.class.getResourceAsStream("/testLO.txt");

        //superuser creates lo
        final LargeObject savedlargeObject = client
                .target(apiRoot + "/large_object")
                .request()
                .header(SESSION_SECRET, superuserClientContext.getSessionSecret())
                .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(LargeObject.class);

        final LargeObject objectWithContent = client
                .target(apiRoot + "/large_object/" + savedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .put(Entity.entity(loStream, MediaType.APPLICATION_OCTET_STREAM))
                .readEntity(LargeObject.class);

        final LargeObject deletedlargeObject = client
                .target(apiRoot + "/large_object/" + objectWithContent.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .delete()
                .readEntity(LargeObject.class);

        int status = client
                .target(apiRoot + "/large_object/" + deletedlargeObject.getId())
                .request()
                .header(SESSION_SECRET, clientContext.getSessionSecret())
                .get()
                .getStatus();

        assertEquals(status, 404);
    }
}
