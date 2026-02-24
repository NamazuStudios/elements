package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.Subjects;
import dev.getelements.elements.sdk.model.system.ElementPathRecordMetadata;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static dev.getelements.elements.rest.test.TestUtils.TEST_API_ROOT;
import static dev.getelements.elements.sdk.model.Headers.SESSION_SECRET;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static org.testng.Assert.*;

public class ElmInspectorResourceIntegrationTest {

    private static final String UPLOAD_URL = "/elm/inspector/upload";

    private static final String ARTIFACT_URL = "/elm/inspector/artifact/{coordinates}";

    private static final String LARGE_OBJECT_URL = "/elm/inspector/large_object/{largeObjectId}";

    @Factory
    public Object[] getTests() {
        return new Object[]{
                TestUtils.getInstance().getTestFixture(ElmInspectorResourceIntegrationTest.class)
        };
    }

    @Inject
    @Named(TEST_API_ROOT)
    private String apiRoot;

    @Inject
    private Client client;

    @Inject
    private Provider<ClientContext> clientContextProvider;

    @Inject
    private LargeObjectDao largeObjectDao;

    @Inject
    private LargeObjectBucket largeObjectBucket;

    private ClientContext superuserContext;

    private ClientContext userContext;

    private Artifact elmArtifact;

    private String elmLargeObjectId;

    @BeforeClass
    private void setUp() throws IOException {

        superuserContext = clientContextProvider.get()
                .createSuperuser("ElmInspectorTestSuperuser")
                .createSession();

        userContext = clientContextProvider.get()
                .createUser("ElmInspectorTestUser")
                .createSession();

        elmArtifact = ElementArtifactLoader.newDefaultInstance()
                .getArtifact(ArtifactRepository.DEFAULTS, JAKARTA_RS.getCoordinatesForElm());

        final var access = new AccessPermissions();
        access.setRead(Subjects.nobody());
        access.setWrite(Subjects.nobody());
        access.setDelete(Subjects.nobody());

        var lo = new LargeObject();
        lo.setPath("/elm/inspector/test/rs.elm");
        lo.setMimeType(ElementPathLoader.ELM_MIME_TYPE);
        lo.setAccessPermissions(access);
        lo = largeObjectDao.createLargeObject(lo);

        elmLargeObjectId = lo.getId();

        try (final var os = largeObjectBucket.writeObject(elmLargeObjectId);
             final var is = Files.newInputStream(elmArtifact.path())) {
            is.transferTo(os);
        }

    }

    // -- Upload tests --

    @Test
    public void testInspectUploadedElm() throws IOException {

        try (final var stream = Files.newInputStream(elmArtifact.path());
             final var multipart = new FormDataMultiPart()) {

            multipart.field("elm", stream, MediaType.APPLICATION_OCTET_STREAM_TYPE);

            final var response = client
                    .target(apiRoot + UPLOAD_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SESSION_SECRET, superuserContext.getSessionSecret())
                    .post(Entity.entity(multipart, multipart.getMediaType()));

            assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

            final var metadata = response.readEntity(new GenericType<List<ElementPathRecordMetadata>>() {});
            assertNotNull(metadata);
            assertFalse(metadata.isEmpty(), "should return at least one element path");

            final var first = metadata.getFirst();
            assertNotNull(first.path(), "path should be populated");
            assertNotNull(first.manifest(), "manifest should be populated");
            assertFalse(first.lib().isEmpty() && first.classpath().isEmpty(),
                    "at least lib or classpath should contain entries");

        }

    }

    @Test
    public void testRegularUserCannotInspectUpload() throws IOException {

        try (final var stream = Files.newInputStream(elmArtifact.path());
             final var multipart = new FormDataMultiPart()) {

            multipart.field("elm", stream, MediaType.APPLICATION_OCTET_STREAM_TYPE);

            final var status = client
                    .target(apiRoot + UPLOAD_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SESSION_SECRET, userContext.getSessionSecret())
                    .post(Entity.entity(multipart, multipart.getMediaType()))
                    .getStatus();

            assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());

        }

    }

    @Test
    public void testUnauthenticatedCannotInspectUpload() throws IOException {

        try (final var stream = Files.newInputStream(elmArtifact.path());
             final var multipart = new FormDataMultiPart()) {

            multipart.field("elm", stream, MediaType.APPLICATION_OCTET_STREAM_TYPE);

            final var status = client
                    .target(apiRoot + UPLOAD_URL)
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(multipart, multipart.getMediaType()))
                    .getStatus();

            assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());

        }

    }

    // -- Artifact tests --

    @Test
    public void testInspectArtifact() {

        final var response = client
                .target(apiRoot + ARTIFACT_URL)
                .resolveTemplate("coordinates", JAKARTA_RS.getCoordinatesForElm())
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var metadata = response.readEntity(new GenericType<List<ElementPathRecordMetadata>>() {});
        assertNotNull(metadata);
        assertFalse(metadata.isEmpty(), "should return at least one element path");

        final var first = metadata.getFirst();
        assertNotNull(first.path(), "path should be populated");
        assertNotNull(first.manifest(), "manifest should be populated");
        assertFalse(first.lib().isEmpty() && first.classpath().isEmpty(),
                "at least lib or classpath should contain entries");

    }

    @Test
    public void testRegularUserCannotInspectArtifact() {

        final var status = client
                .target(apiRoot + ARTIFACT_URL)
                .resolveTemplate("coordinates", JAKARTA_RS.getCoordinatesForElm())
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, userContext.getSessionSecret())
                .get()
                .getStatus();

        assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());

    }

    @Test
    public void testUnauthenticatedCannotInspectArtifact() {

        final var status = client
                .target(apiRoot + ARTIFACT_URL)
                .resolveTemplate("coordinates", JAKARTA_RS.getCoordinatesForElm())
                .request(MediaType.APPLICATION_JSON)
                .get()
                .getStatus();

        assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());

    }

    // -- Large object tests --

    @Test
    public void testInspectLargeObject() {

        final var response = client
                .target(apiRoot + LARGE_OBJECT_URL)
                .resolveTemplate("largeObjectId", elmLargeObjectId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, superuserContext.getSessionSecret())
                .get();

        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        final var metadata = response.readEntity(new GenericType<List<ElementPathRecordMetadata>>() {});
        assertNotNull(metadata);
        assertFalse(metadata.isEmpty(), "should return at least one element path");

        final var first = metadata.getFirst();
        assertNotNull(first.path(), "path should be populated");
        assertNotNull(first.manifest(), "manifest should be populated");
        assertFalse(first.lib().isEmpty() && first.classpath().isEmpty(),
                "at least lib or classpath should contain entries");

    }

    @Test
    public void testRegularUserCannotInspectLargeObject() {

        final var status = client
                .target(apiRoot + LARGE_OBJECT_URL)
                .resolveTemplate("largeObjectId", elmLargeObjectId)
                .request(MediaType.APPLICATION_JSON)
                .header(SESSION_SECRET, userContext.getSessionSecret())
                .get()
                .getStatus();

        assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());

    }

    @Test
    public void testUnauthenticatedCannotInspectLargeObject() {

        final var status = client
                .target(apiRoot + LARGE_OBJECT_URL)
                .resolveTemplate("largeObjectId", elmLargeObjectId)
                .request(MediaType.APPLICATION_JSON)
                .get()
                .getStatus();

        assertEquals(status, Response.Status.FORBIDDEN.getStatusCode());

    }

}